# Repository Guidelines

## Project Structure & Module Organization
`src/` contains the application code. Use `views/` for routed pages (`auth/`, `main/`, `errors/`), `components/` for shared UI, `layouts/` for page shells, `stores/` for Pinia state, `services/` for API clients, `config/` for static app config, and `utils/` for helpers. Global styles live in `src/style.css` and `src/assets/css/`. Static files that must keep their URL go in `public/`; bundled assets go in `src/assets/`. `dist/` is build output and should not be edited manually.

## Build, Test, and Development Commands
Run `npm install` to install dependencies. Use `npm run dev` for local development with the Vite dev server. Use `npm run build` to create the production bundle in `dist/`. Use `npm run preview` to serve the built bundle locally for a final check. The dev server proxies `/api` to `http://localhost:8080`, so keep the backend running when testing integrated flows.

## Coding Style & Naming Conventions
This repo uses Vue 3 SFCs, Pinia, and `@` as an alias to `src/`. Name components, layouts, and view files in PascalCase (`AppSidebar.vue`, `UserManagement.vue`). Keep route paths in kebab-case and service modules in the existing `*.service.js` pattern (`auth.service.js`). Prefer 2-space indentation in templates, scripts, and CSS, but preserve the surrounding style in files that already differ. Avoid large formatting-only diffs. Keep API access inside `src/services/` rather than calling `axios` directly from views.

## Testing Guidelines
No automated test runner is configured yet. Until one is added, treat `npm run build` as the minimum validation step and manually smoke-test the affected route, API call, and permission flow. For UI changes, verify both authenticated and unauthenticated navigation when relevant. If you add tests later, use clear `*.spec.js` names and keep them close to the feature they cover.

## Commit & Pull Request Guidelines
Recent history uses short, imperative commit subjects such as `Fix UI`, `Fix Data`, and `Fix role`. Keep commits focused and use the same style, but make the scope more specific when possible, for example `Fix payroll batch filter`. Pull requests should include a short summary, impacted screens or services, linked issue or task ID, and screenshots for visible UI changes. Note any backend dependency or API contract change in the PR description.

## Configuration Tips
Keep secrets and environment-specific endpoints out of committed source. When changing routing, menu config, or permissions, update the related view, `src/router/index.js`, and any affected service/store together so navigation and access control stay consistent.
