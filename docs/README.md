# Pokedex Technical Architecture Diagram

This directory contains the technical architecture documentation for the Pokedex Kotlin Multiplatform (KMP) application.

The architecture is represented as a layered technical diagram built using the **Cocoon-AI Architecture Diagram Design System** format.

## Architecture Diagram

The diagram is saved as a self-contained, interactive HTML file that can be opened directly in any modern web browser:

- 📊 **Architecture Diagram** — [architecture.html](file:///Users/kevs/Documents/workshop/Android/pokedex/docs/architecture.html)

### Features of the Interactive Diagram
- **Modern Dark-Themed Aesthetic:** Clean visualization with HSL/RGB colors, JetBrains Mono font, and a precise grid system.
- **Full KMP Layer Representation:** Clearly shows how the platforms (Android, iOS, Web) bootstrap the shared codebase, and details the internal layers (Presentation, Domain, Data, Infrastructure, Dependency Injection).
- **Export Toolbar:** Built-in actions to download the diagram as a high-DPI PNG or PDF, or copy the template HTML.

### How to View
To view and interact with the architecture diagram:

#### Option 1: Serve locally (Recommended)
To prevent browser-level security policies (CORS and Same-Origin Policies) from restricting `html2canvas` canvas data exports on `file://` protocols:
1. In your terminal, run the serve task from the project root:
   ```bash
   task serve-docs
   ```
2. Open [http://localhost:8085/architecture.html](http://localhost:8085/architecture.html) in your browser.

#### Option 2: Open file directly
1. Open the [architecture.html](file:///Users/kevs/Documents/workshop/Android/pokedex/docs/architecture.html) file.
2. Right-click the file in your project explorer and select **Open in Browser** (or copy its absolute path and open it in your browser).
   *(Note: Interactive export features might be blocked by the browser's sandbox when using the local `file:///` scheme).*


