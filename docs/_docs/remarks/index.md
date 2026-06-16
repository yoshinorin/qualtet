---
layout: doc-page
title: Remarks
---

## Private Key for JWT

Qualtet uses an asymmetric keypair for generating/validating JWT. The keypair source is configurable (`IN-MEMORY`, `FILE`, or `PEM`).

By default (`IN-MEMORY`) the keypair is generated when booting the HTTP server (Qualtet) and is not persisted. This means existing JWTs become invalid whenever the HTTP server reboots. Use the `FILE` or `PEM` source to load a stable keypair so JWTs survive reboots.

See the Getting Started page (the KeyPair section) for the configuration.

## Need Entropy

This only applies when the keypair source is `IN-MEMORY` (the default). The `FILE` and `PEM` sources load an existing key and do not need entropy.

When generating the in-memory keypair, Qualtet requires entropy. By default it uses `SecureRandom.getInstanceStrong`, which may block on boot on low-entropy systems (e.g. freshly booted containers / VMs). For more details, please see below StackOverflow's answer.

> See: [/dev/random Extremely Slow?](https://stackoverflow.com/questions/4819359/dev-random-extremely-slow)

If boot is slow because of this, you can either:

- set `QUALTET_KEYPAIR_SECURE_RANDOM` to a non-blocking algorithm (e.g. `NativePRNGNonBlocking`), or
- switch to the `FILE` / `PEM` source so no key generation happens at boot.

## Markdown Parsing

Qualtet **does not parse markdown to HTML**. Please parse it and set it to the `htmlContent` field yourself when you `POST` content.

## Escape HTML

Qualtet **does not escape HTML**. Please escape it and set it to the `htmlContent` field yourself when you `POST` content if needed.
