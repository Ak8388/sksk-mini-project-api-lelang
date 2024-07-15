## Cara Install 
- Install JDK 21 : https://download.oracle.com/java/21/latest/jdk-21_windows-x64_bin.zip
- Cek Java version
``` 
$ java --version
openjdk 21.0.3 2024-04-16 LTS
```
- Install Maven : https://maven.apache.org/download.cgi
- Cek Maven version
```
$ mvn --version
Apache Maven 3.9.8
```

## Cara Menjalankan Program

* Jalankan database migration tools
```bash
./mvnw clean flyway:migrate -Dflyway.configFiles=tools/db/migrations.conf
```
## Jika menggunakan windows 
``` 
jalanakan perintah menggunakan gitbash comand atau cmd "mvn clean flyway:migrate -Dflyway.configFiles=tools/db/migrations.conf"
```

* Jalankan aplikasi
```bash
./mvnw spring-boot:run 
```

## Pengujian API dengan Postman
```
Cara install : https://www.postman.com/downloads/
```
## Login

### Endpoint
```
POST |  "http://localhost:8081/login"
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
GET |  "http://localhost:8081/secured/user/list"
```
```
GET |  "http://localhost:8081/secured/user/list?page=2&size=5"
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

## Register 
* Seller : 
### Endpoint
```
POST |  "http://localhost:8081/secured/user/register-seller"
```
- Header
```
Authorization : Bearer <token admin>
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
POST |  "http://localhost:8081/secured/user/register-buyer"
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
POST | "http://localhost:8081/secured/user/reset-password"
```
- Header
```
Authorization : Bearer <token Admin User>
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

## Update Profile : 
### Endpoint
```
POST |
"http://localhost:8081/secured/user/update-profile"
```
- Header
```
Authorization : Bearer <token Admin User>
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

## Delete User 
### Seller : 
- Endpoint
```
POST | "http://localhost:8081/secured/user/delete-user"

```
- Header
```
Authorization : Bearer <token Admin>
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
    "code": "0600",
    "message": "Berhasil Menghapus",
    "data": 3
    }
    ```

### Buyer: 
- Endpoint
```
POST | "http://localhost:8081/secured/user/delete-user"
```
- Header
```
Authorization : Bearer <token Admin>
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
    "code": "0600",
    "message": "Berhasil Menghapus",
    "data": 3
    }
    ```

## Auction
### Create : 
- Endpoint
```
POST | "http://localhost:8081/secured/auction/create-auction"

```
- Header
```
Authorization : Bearer <token Seller>
```
- body : 
``` 
{
  "name":"Lukisan Watermelon Sugar",
  "description":"pelelangan lukisan watermelon sugar, inspired by twinkling watermelon",
  "minimumPrice":"2000000",
  "maximumPrice":"200000000",
  "startedAt":"2024-07-20T20:00:45.123+07:00",
  "endedAt":"2024-07-21T09:00:45.123+07:00"
}
```
### Response 
- sukses
    ``` 
        {
        "code": "2001",
        "message": "sukses membuat pengajuan lelang",
        "data": 12
        }
    ```
- validasi waktu,
    ``` 
        {
        "code": "4000",
        "message": "waktu lelang tidak boleh kurang atau sama dengan hari ini",
        "data": null
        }
    ```

### List : 
-Endpoint
```
GET | "http://localhost:8081/secured/auction/list-auction"

Get | "http://localhost:8081/secured/auction/list-auction?status=APPROVED&page=1&size=1"

```
- Header
```
Authorization : Bearer <token Admin>
```
### Response

- sukses
    ```
    {
    "code": "2001",
    "message": "success get data",
    "data": {
        "totalData": 3,
        "totalPage": 1,
        "page": 1,
        "offset": 10,
        "auctionData": [
        {
            "id": 9,
            "name": "lelang baju PUB",
            "description": "baju ini bekas di pake oleh mamng gufron sh mh mah mah",
            "offer": 245000,
            "highestBid": 12000000,
            "highestBidderId": 3,
            "highestBidderName": "hafiz",
            "status": "APPROVED",
            "startedAt": "2024-07-10T23:10:45.123+07:00",
            "endedAt": "2024-07-15T14:32:45.123+07:00"
        }
        ]
        }
    }
    ``` 
### Bid Auction : 
- Endpoint
```
POST | "http://localhost:8081/secured/auction/bid-lelang"
```
- Header
```
Authorization : Bearer <token buyer>
```
- body : 
```
{
"auctionID":"12",
"highestBid":"99000000"
}
```
### Respon
- sukses

    ```
    {
    "code": "0107",
    "message": "sukses bid lelang",
    "data": [
        "auctionID":"12",
        "highestBid":"99000000"
    ]
    }
    ```


### Rejected :
- Endpoint
```
POST | "http://localhost:8081/secured/auction/reject-auction?=id"

```
- Header
```
Authorization : Bearer <token Admin>
```
### Response

 - sukses
    ```
        {
        "code": "0700",
        "message": "Auction Rejected",
        "data": 12
        }
    ```

### Approved : 
- Endpoint
```
POST | "http://localhost:8081/secured/auction/approve-auction?=id"

```
- Header
```
Authorization : Bearer <token Admin>
```
### Response
- sukses
    ```
        {
            "code": "0500",
            "message": "Auction Approved successfully",
            "data": 12
        }
    ```


