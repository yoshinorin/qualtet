---
layout: doc-page
title: Remarks
---

## Private Key for JWT

Qualtet has a private-key with an in-memory for generating/validating JWT. It's generated when booting the http-server (qualtet). It means JWT will be invalid if the http-server reboot.

## Need Entropy

Qualtet require entropy to create a private-key. For more details, please see below StackOverflow's answer.

> See: [/dev/random Extremely Slow?](https://stackoverflow.com/questions/4819359/dev-random-extremely-slow)

## Markdown Parsing

Qualtet **does not parse markdown to HTML**. Please parse it and set to `htmlContent` field by yourself when you `POST` a content.

## Escape HTML

Qualtet **does not escape HTML**. Please escape it and set to `htmlContent` field by yourself when you `POST` a content if you need.
