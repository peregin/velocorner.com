StatusPage Updater
---

Go Service
- collects health requests form the downstreams
- sends component health requests periodically

Go Lambda
- checks last status updates on the components, if more than double the frequency than sets the component to failed