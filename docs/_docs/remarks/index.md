---
layout: doc-page
title: Remarks
---

## Private Key for JWT

Qualtet has an in-memory private key for generating/validating JWT. It's generated when booting the HTTP server (Qualtet). This means JWT will be invalid if the HTTP server reboots.

## Need Entropy

Qualtet require entropy to create a private-key. For more details, please see below StackOverflow's answer.

> See: [/dev/random Extremely Slow?](https://stackoverflow.com/questions/4819359/dev-random-extremely-slow)

## Markdown Parsing

Qualtet **does not parse markdown to HTML**. Please parse it and set it to the `htmlContent` field yourself when you `POST` content.

## Escape HTML

Qualtet **does not escape HTML**. Please escape it and set it to the `htmlContent` field yourself when you `POST` content if needed.
