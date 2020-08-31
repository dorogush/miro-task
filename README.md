# Miro
This is a test task for a backend developer from [miro.com](https://miro.com).

## Prerequisites
* OpenJDK 11

## Run
```
./mvnw spring-boot:run
```

## Widget model
*Field* | *Type* | *Read only* | *Mandatory for create* | *Comment*
---|---|---|---|---
id | String | true | - | App will auto generate it.
x | Integer | false | true | X-index.
y | Integer | false | true | Y-index.
z | Integer | false | false | Z-index.
width | Integer | false | true | Width.
height | Integer | false | true | Height.
lastModified | Integer | true | - | Last modified timestamp. Milliseconds since Epoch.

## REST endpoint

### Create
```
POST /widgets
```
Example
```
curl \
-H "Content-Type: application/json" \
-H "Accept: application/json" \
-X POST http://127.0.0.1:8080/widgets \
-d '{
  "x": 1,
  "y": 2,
  "width": 3,
  "height": 4
}'
```

### Read one
```
GET /widgets/{widgetId}
```
Example
```
curl \
-H "Accept: application/json" \
-X GET http://127.0.0.1:8080/widgets/4b765509-07f5-476a-8169-66ddeac7f39f
```

### Read all
```
GET /widgets
```
Example
```
curl \
-i \
-H "Accept: application/json" \
-X GET http://127.0.0.1:8080/widgets?perPage=2
```
If there are more widgets available, in response you will get header `Link`.
Example
```
Link: <http://127.0.0.1:8080/widgets?perPage=2&fromZ=3>; rel="next"
```
Use this link to fetch the next page.

### Update
```
PUT /widgets/{widgetId}
```
Example
```
curl \
-H "Content-Type: application/json" \
-H "Accept: application/json" \
-X PUT http://127.0.0.1:8080/widgets/ae6c4ec9-36e7-498b-9ea6-4656d2b85a93 \
-d '{
  "x": 1
}'
```
Only specific fields provided in the payload will be updated.

### Delete
```
DELETE /widgets/{widgetId}
```
Example
```
curl \
-i \
-H "Content-Type: application/json" \
-H "Accept: application/json" \
-X DELETE http://127.0.0.1:8080/widgets/ae6c4ec9-36e7-498b-9ea6-4656d2b85a93
```

## Spring profiles
There are 2 spring profiles `prod` (default) and `dev`.
The main difference is logging configuration:
* `prod` outputs in json format and all levels to `info`.
* `dev` outputs in plain text and `com.adorogush.mirotask` level is `debug`.

## Configuration properties
*Key* | *Default value* | *Description*
---|---|---
perPageDefault | 10 | Items per page when "perPage" query parameter not specified.
perPageMax | 500 | Max page size.
rateLimit.global.enabled | true | If global rate limit enabled.
rateLimit.global.rpm | 1000 | Global rate limit requests per minute value.
rateLimit.create.enabled | false | If create operation rate limit enabled.
rateLimit.create.rpm | 1000 | Rate limit for create operations (requests per minute).
rateLimit.readOne.enabled | false | If readOne rate limit enabled.
rateLimit.readOne.rpm | 1000 | Rate limit for readOne operations (requests per minute).
rateLimit.readAll.enabled | false | If readAll rate limit enabled.
rateLimit.readAll.rpm | 1000 | Rate limit for readAll operations (requests per minute).
rateLimit.update.enabled | false | If update rate limit enabled.
rateLimit.update.rpm | 1000 | Rate limit for update operations (requests per minute).
rateLimit.delete.enabled | false | If delete rate limit enabled.
rateLimit.delete.rpm | 1000 | Rate limit for delete operations (requests per minute).

## Dynamic properties
Any property can be changed without application restart using Actuator endpoint. For example:
```
curl \
-H "Content-Type: application/json" \
-H "Accept: application/json" \
-X POST http://127.0.0.1:8080/actuator/env \
-d '{"name":"rateLimit.global.enabled","value":"true"}'
```

## Rate limiting
Rate limit can be configured globally or specifically per operation. Current rate limit status is part of each response headers:

*Header* | *Description*
---|---
X-Requests-Per-Minute | Current configuration of rate limit for this operation. In requests per minute
X-Requests-Available | Currently available remaining operations.
X-Nanos-Until-Refill | Nanoseconds until the available remaining operations bucket will refill.

## Code style
This project follows [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
