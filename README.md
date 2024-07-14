
### Cara Menjalankan Program


* Jalankan database migration tools
```bash
./mvnw clean flyway:migrate -Dflyway.configFiles=tools/db/migrations.conf
```

* Jalankan aplikasi
```bash
./mvnw spring-boot:run 
```
## Login

### Endpoint
```
POST |  "http://localhost:8080/login"
```
- body : 
``` 
{
    "email"    : "charlie@example.com",
    "password" : "12345678"
}
```
### Response
- sukses
```
{
  "code": "0800",
  "message": "Sukses",
  "data": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjEsInJvbGUiOiJBRE1JTiIsIm5hbWUiOiJBZG1pbiIsImlhdCI6MTcyMDgwNDI3MDA4MiwiZXhwIjo4NjQwMDAwMH0.1cMGJs8yRoQYH4sYWitSFsM59Vhfwrlq6P5Tj-RVtDM"
}
```
- salah password/email
```
{
  "code": "0802",
  "message": "Email atau password salah",
  "data": null
}
```

## List User :
### Endpoint 
```
GET |  "http://localhost:8080/secured/user/list"
```
```
GET |  "http://localhost:8080/secured/user/list?page=&&"
```
- Header
```
Authorization : Bearer <token User (ADMIN/SELLER/BUYER)>
```


### Response

- sukses
```
{
  "code": "0900",
  "message": "Sukses",
  "data": [
    {
      "name": "Admin",
      "role": "ADMIN"
    }
  ]
}
```
- pagination
```
{
    "id":"1",
    "name":"Charlie",
    "role":"ADMIN"
}
```
- null
```
{
    "id":"1",
    "name":"Charlie",
    "role":"ADMIN"
}
```

## Register 
* Seller : 
### Endpoint
```
POST |  "http://localhost:8080/secured/user/register-seller"
```
- Header
```
Authorization : Bearer <token admin >
```

- body : 
``` 
{
  "name":"Rara",
  "email":"rara@gmail.com",
  "password":"passwordrara"
}
```
### Response
 - sukses
  ``` 
{
  "code": "0500",
  "message": "Sukses",
  "data": 3
}
```
* Buyer : 
### Endpoint
```
POST |  "http://localhost:8080/secured/user/register-buyer"
```
- Header
```
Authorization : Bearer <token admin >
```

- body : 
``` 
{
  "name":"boni",
  "email":"boni@gmail.com",
  "password":"bonipassword"
}
```
### Response
 - sukses
 ``` 
{
  "code": "0600",
  "message": "Sukses",
  "data": 4
}
```

## Reset Password : 
### Endpoint
```
POST | "http://localhost:8080/secured/user/reset-password"
```
- Header
```
Authorization : Bearer <token Admin atau User itu Sendiri : Seller/Buyer>
```

- body : 
``` 
{
    "oldPassword" : "12345678",
    "NewPassword" : "2345678"
}
```
### Response
- sukses
``` 
{
    "email"    : "charlie@example.com",
    "password" : "12345678",
    "role"     : "BUYER"
}
```
- wrong old password
``` 
{
    "email"    : "charlie@example.com",
    "password" : "12345678",
    "role"     : "BUYER"
}
```

- password baru sama lama sama
``` 
{
    "email"    : "charlie@example.com",
    "password" : "12345678",
    "role"     : "BUYER"
}
```

- user not found
``` 
{
    "email"    : "charlie@example.com",
    "password" : "12345678",
    "role"     : "BUYER"
}
```

-akun sudah di hapus
```
{
  "code": "0706",
  "message": "Account has been deleted",
  "data": null
}
```

- badrequest
``` 
{
    "email"    : "charlie@example.com",
    "password" : "12345678",
    "role"     : "BUYER"
}
```

## Update Profile : 
### Endpoint
```
POST |
"http://localhost:8080/secured/user/update-profile"
```
- Header
```
Authorization : Bearer <token Admin atau User itu Sendiri : Seller/Buyer>
```

- body  
``` 
{
  "name":"charlie",
  "email":"charlie@gmail.com"
}
```

- sukses 
``` 
{
  "code": "0600",
  "message": "sukses update profile",
  "data": 1
}
```

- badrequest
``` 
{
    "nama" : "12345678",
    "email" : "2345678"
}
```

## Delete User 
### Seller : 
- Endpoint
```
POST | "http://localhost:8081/secured/user/delete-user"

```
- Header
```
Authorization : Bearer <token User>
```
- body : 
``` 
{
    "id" : "1"
}
```
### Response
- sukses
    ```
    {
        "message" : "12345678",
        "email" : "2345678"
    }
    ```
- not found user
    ```
    {
        "message" : "12345678",
        "email" : "2345678"
    }
    ```
- unauthorized
```
{
  "code": "0201",
  "message": "unauthorized",
  "data": null
}
```

### Buyer: 
- Endpoint

    - sukses
    - not found user


## Auction
### Create : 
- Endpoint
```
POST | "http://localhost:8080/create-auction"

```
- Header
```
Authorization : Bearer <token Seller>
```
- body : 
``` 
{
    "id" : "1"
}
```
### Response 
- sukses
    ``` 
        {
            "id" : "1"
        }
    ```
- badrequest,
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```

### List : 
- Endpoint
    - semua data,
    - pagination, 
    - null

### Bid Auction : 
- Endpoint
    - sukses, 

### Update High Bid : 
- Endpoint
    - sukses,

### Rejected :
- Endpoint
```
POST | "http://localhost:8080/secured/auction/reject?=id"

```
- Header
```
Authorization : Bearer <token Admin>
```
### Response

 - sukses
    ```
        {
            "message" : "1",
            "data:"null",
        }
    ```

 - badrequest
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```
- not found
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```

### Approved : 
- Endpoint
```
POST | "http://localhost:8080/approve?=id"

```
- Header
```
Authorization : Bearer <token Admin>
```
### Response
- sukses
    ```
        {
            "message" : "1",
            "data:"null",
        }
    ```

- badrequest
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```
- not found
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```

### Bid Lelang
- Endpoint
```
{
POST | "http://localhost:8080/secured/auction/bid-lelang"
}
```

- Header
```
Authorization : Bearer <token Admin>
```
### Response
- sukses
    ```
        {
            "message" : "1",
            "data:"null",
        }
    ```

- badrequest
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```
- not found
    ``` 
        {
            "message" : "1",
            "data:"null",
        }
    ```


