// This runs before Jest environment is set up
// Set up globals that Expo SDK 54 winter runtime needs
globalThis.__ExpoImportMetaRegistry = {};
globalThis.structuredClone = globalThis.structuredClone || ((obj) => JSON.parse(JSON.stringify(obj)));
