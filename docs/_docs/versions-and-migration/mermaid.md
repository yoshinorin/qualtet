**NOTE: This file is not included in `sbt doc` command result. Please export the preview result of `Mermaid.js` as an image file and use that instead.**

```mermaid
stateDiagram-v2
    [*] --> not_required
    [*] --> unapplied
    unapplied --> in_progress

    in_progress --> failed
    in_progress --> success

    failed --> in_progress
```
