---
layout: doc-page
title: Remarks
---

## Private Key for JWT

Qualtet has a private-key with an in-memory for generating/validating JWT. It's generated when booting the http-server (qualtet). It means JWT will be invalid if the http-server reboot.

## Escape HTML

Qualtet **does not escape HTML** when creating or updating a `htmlContent` field when you `POST` a content (JSON). Please escape it yourself before `POST`.
