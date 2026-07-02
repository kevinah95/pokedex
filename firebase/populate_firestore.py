import os
import time
import requests
import argparse
from google.cloud import firestore

parser = argparse.ArgumentParser(description="Populate Firestore with Pokemon data.")
parser.add_argument("--prod", action="store_true", help="Populate production Firestore instead of local emulator.")
parser.add_argument("--project", type=str, default="pokedex", help="The Google Cloud project ID (defaults to 'pokedex').")
args = parser.parse_args()

if not args.prod:
    # Force emulator host before initializing client if not in prod mode
    os.environ["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080"

def get_pokemon_id_from_url(url):
    return int(url.strip("/").split("/")[-1])

def get_home_sprite_url(pokemon_id):
    return f"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/{pokemon_id}.png"

def fetch_with_retry(url, retries=5, backoff_factor=2.0):
    for i in range(retries):
        try:
            resp = requests.get(url)
            if resp.status_code == 200:
                return resp
            print(f"Warning: Received status code {resp.status_code} for {url}. Retrying in {backoff_factor ** i} seconds...")
        except requests.RequestException as e:
            print(f"Warning: Request failed ({e}) for {url}. Retrying in {backoff_factor ** i} seconds...")
        time.sleep(backoff_factor ** i)
    # Final attempt that will raise status if it fails
    resp = requests.get(url)
    resp.raise_for_status()
    return resp

def parse_evolution_chain(chain_data):
    stages = []
    
    def traverse(node, trigger=None):
        species_name = node["species"]["name"]
        species_url = node["species"]["url"]
        pokemon_id = get_pokemon_id_from_url(species_url)
        
        stages.append({
            "number": pokemon_id,
            "name": species_name.capitalize(),
            "imageUrl": get_home_sprite_url(pokemon_id),
            "evolutionTrigger": trigger
        })
        
        for next_node in node.get("evolves_to", []):
            next_trigger = "Evolve"
            details = next_node.get("evolution_details", [])
            if details:
                detail = details[0]
                trigger_name = detail["trigger"]["name"]
                if trigger_name == "level-up":
                    min_level = detail.get("min_level")
                    next_trigger = f"Level {min_level}" if min_level else "Level Up"
                elif trigger_name == "trade":
                    next_trigger = "Trade"
                elif trigger_name == "use-item":
                    item_name = detail["item"]["name"].replace("-", " ").title()
                    next_trigger = f"Use {item_name}"
            
            traverse(next_node, next_trigger)
            
    traverse(chain_data["chain"])
    return stages

def main():
    if args.prod:
        print(f"Connecting to production Firestore for project '{args.project}'...")
    else:
        print(f"Connecting to Firestore emulator at 127.0.0.1:8080 for project '{args.project}'...")
        
    db = firestore.Client(project=args.project)
    
    print("Fetching first generation Pokemon list...")
    list_url = "https://pokeapi.co/api/v2/pokemon?limit=151"
    r = fetch_with_retry(list_url)
    results = r.json()["results"]
    
    # Caches to avoid redundant HTTP requests
    species_cache = {}
    evolution_cache = {}
    
    for item in results:
        url = item["url"]
        pokemon_id = get_pokemon_id_from_url(url)
        
        # Check if already exists in local Firestore
        doc_ref = db.collection("pokemons").document(str(pokemon_id))
        if doc_ref.get().exists:
            print(f"[{pokemon_id}/151] Skipping existing pokemon: {item['name'].capitalize()}")
            continue
            
        print(f"[{pokemon_id}/151] Fetching details for {item['name']}...")
        detail_url = f"https://pokeapi.co/api/v2/pokemon/{pokemon_id}"
        detail_resp = fetch_with_retry(detail_url)
        detail_data = detail_resp.json()
        
        # 1. Basic properties
        name = detail_data["name"].capitalize()
        types = [t["type"]["name"] for t in detail_data["types"]]
        height = float(detail_data["height"])
        weight = float(detail_data["weight"])
        
        # 2. Stats
        stat_map = {
            "hp": "HP",
            "attack": "ATK",
            "defense": "DEF",
            "special-attack": "SATK",
            "special-defense": "SDEF",
            "speed": "SPD"
        }
        stats = []
        for s in detail_data["stats"]:
            api_name = s["stat"]["name"]
            if api_name in stat_map:
                stats.append({
                    "name": stat_map[api_name],
                    "value": int(s["base_stat"])
                })
        
        # Sort stats to match target HP, ATK, DEF, SATK, SDEF, SPD order
        order = ["HP", "ATK", "DEF", "SATK", "SDEF", "SPD"]
        stats.sort(key=lambda x: order.index(x["name"]) if x["name"] in order else 99)
        
        # 3. Species info (genera & evolution chain URL)
        species_url = detail_data["species"]["url"]
        if species_url not in species_cache:
            species_resp = fetch_with_retry(species_url)
            species_data = species_resp.json()
            
            # Extract genus
            species_genus = "Unknown"
            for g in species_data.get("genera", []):
                if g["language"]["name"] == "en":
                    species_genus = g["genus"]
                    break
            
            evolution_url = species_data["evolution_chain"]["url"]
            species_cache[species_url] = (species_genus, evolution_url)
        
        species_genus, evolution_url = species_cache[species_url]
        
        # 4. Evolution chain info
        if evolution_url not in evolution_cache:
            evol_resp = fetch_with_retry(evolution_url)
            evolution_cache[evolution_url] = parse_evolution_chain(evol_resp.json())
            
        evolution_chain = evolution_cache[evolution_url]
        
        # Create Firestore document
        pokemon_doc = {
            "number": pokemon_id,
            "name": name,
            "imageUrl": get_home_sprite_url(pokemon_id),
            "types": types,
            "species": species_genus,
            "height": height,
            "weight": weight,
            "stats": stats,
            "evolutionChain": evolution_chain
        }
        
        # Write to local firestore emulator
        doc_ref.set(pokemon_doc)
        
        # Pacing delay to respect public API rate limits
        time.sleep(0.2)
        
    print("Database seeding completed successfully!")

if __name__ == "__main__":
    main()
