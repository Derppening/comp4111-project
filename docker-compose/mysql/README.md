# docker-compose for MySQL

This directory contains a standard `docker-compose` script for hosting a MySQL server instance.

## How to Use

### Starting the container

To create and start the container:

```sh
docker-compose up -d
```

### Entering the container environment

To enter the container environment:

```sh
docker-compose attach mysql
```

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

By default, the `root` user of the MySQL server is set to `comp4111`. To change this, edit the following line in 
`docker-compose.yml`:

```diff
     environment:
-      - MYSQL_ROOT_PASSWORD=comp4111
+      - MYSQL_ROOT_PASSWORD=mynewpass
```