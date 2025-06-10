How to run (specify your API KEY in Dockerfile):
```
docker build -t gemini-integrator-doh-image .
```
```
docker run --rm --name integrator-gemini-doh --dns 8.8.8.8 gemini-integrator-doh-image
```
```
docker cp integrator-gemini-doh:/app C:\temp\output
```

