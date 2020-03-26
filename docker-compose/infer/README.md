# docker-compose for Infer

This directory contains a standard `docker-compose` script for starting Infer.

## How to Use

### Starting the container

To create and start the container:

```sh
docker-compose up -d --build
```

Note that this process may take a while.

### Entering the container environment

To enter the container environment:

```sh
docker-compose attach infer
```

When attaching to a container, files in the current project root will be copied into the container. This is to avoid 
Gradle creating files as the `root` user in the project, which will cause `Permission denied` errors when running Gradle
with the normal user.

Special Paths:

- `/mnt`: Read-only view of the project files in the host filesystem
- `/root/project`: Working directory for running Infer on the project

### Stopping the container

To stop the container:

```sh
docker-compose stop
```

### Removing the container

To remove the container:

```sh
docker-compose down
```

### Customization

By default, the root of the current project (`../../`) is used as the project root. To use another directory as the 
project root, edit the line in `docker-compose.yml`:

```diff
     volumes:
       - type: bind
-        source: ../../
+        source: /path/to/dir
         target: /mnt
         read_only: true
```
