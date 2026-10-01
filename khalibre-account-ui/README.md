# Custom Keycloak Account UI

This is a template to build a custom Keycloak Account UI using the [Keycloak Account UI](https://npmjs.com/package/@keycloak/keycloak-account-ui) package.

## Getting started

In this repository, the quickest way to run the UI with HMR against the dockerized Keycloak
is the mise task, which wires `KC_ACCOUNT_VITE_URL` for you:

```bash
mise run dev:account-ui        # from the repository root
mise run dev:account-ui:stop   # back to the bundled theme JAR
```

Alternatively, to set the Vite dev server up manually, run the following command:

```bash
pnpm i
pnpm run dev
```
Then start the Keycloak server:

```bash
pnpm run start-keycloak
```

open the admin-console in your browser (http://localhost:8080/), when you the go to the `Manage Account` by clicking on the name in the toolbar, you will see the custom UI.

## Build

To build the application for production, run the following command:

```bash
mvn install
```
This will create a "jar" file in the `target` directory that you can deploy to your Keycloak server by copying it to the `providers` directory.