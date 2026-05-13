# Gioi thieu cong nghe he thong - Vantix-Pro

## 1. Tong quan kien truc he thong

Du an **Vantix-Pro** duoc xay dung theo mo hinh **Client-Server**, trong do he thong duoc tach thanh hai thanh phan chinh:

- **Client (Front-end):** ung dung web don trang (Single Page Application - SPA) phat trien bang Vue 3, chiu trach nhiem hien thi giao dien, quan ly trang thai phia nguoi dung va tuong tac voi nguoi su dung.
- **Server (Back-end):** ung dung Spring Boot dong vai tro xu ly nghiep vu, xac thuc, phan quyen, truy xuat du lieu va cung cap cac dich vu API cho frontend.

Xet tren goc do to chuc ma nguon backend, he thong the hien ro mo hinh **phan lop (Layered Architecture)**, bao gom:

- **Controller layer:** tiep nhan request HTTP va tra ve response duoi dang JSON thong qua cac `@RestController`.
- **Service layer:** xu ly logic nghiep vu.
- **Repository/Data Access layer:** lam viec voi co so du lieu thong qua Spring Data JPA.
- **Entity/Model layer:** bieu dien cau truc du lieu nghiep vu va anh xa voi cac bang trong CSDL.

Ve ban chat, day la su ket hop giua **kien truc Client-Server** o muc he thong va **kien truc phan lop gan voi MVC/MV* hien dai** o muc trien khai backend. Tuy backend su dung `spring-boot-starter-webmvc`, nhung trong thuc te du an khong di theo huong MVC sinh giao dien server-side ma chuyen trong tam sang **RESTful service architecture**, voi frontend va backend tach biet ro rang.

### Ly do lua chon

- **Client-Server** giup tach biet giao dien va xu ly nghiep vu, tu do de bao tri, de mo rong va ho tro phat trien song song giua cac nhom FE/BE.
- **Layered Architecture** giup phan tach trach nhiem ro rang, tang tinh tai su dung va lam cho he thong de kiem soat chat luong ma nguon.
- **SPA + REST API** phu hop voi bai toan quan tri nhan su vi can trai nghiem giao dien linh hoat, cap nhat nhanh va tuong tac nhieu voi du lieu.

## 2. Cong nghe Front-end

Frontend chinh cua he thong nam tai module **`Vantix_Web`**, duoc xac nhan qua file `package.json` va `vite.config.js`.

### Ngon ngu va nen tang

- **JavaScript (ES Modules)** la ngon ngu lap trinh phia frontend.
- Du an cau hinh `"type": "module"` trong `package.json`, cho thay frontend su dung co che **ESM - ECMAScript Modules** hien dai.

### Framework chinh

- **Vue.js 3** (`vue`)

Vue 3 la framework JavaScript hien dai, phu hop cho viec xay dung giao dien thanh phan hoa (component-based), reactivity tot va de phat trien cac man hinh quan tri co nhieu form, bang du lieu va thao tac nghiep vu.

### Cong cu build va phat trien

- **Vite**
- **@vitejs/plugin-vue**

Vite duoc su dung lam cong cu dev server va build frontend. Cau hinh trong `vite.config.js` cho thay frontend chay o cong `3000`, dong thoi proxy cac request `/api` va `/ws` sang backend tai cong `8080`.

#### Uu diem

- Toc do khoi dong nhanh.
- Ho tro Hot Module Replacement (HMR) tot.
- Cau hinh gon nhe, de tich hop voi Vue 3.

### Thu vien UI va ho tro giao dien

- **Element Plus**
- **@element-plus/icons-vue**

Element Plus la bo thu vien UI danh cho Vue 3, cung cap san cac thanh phan quan tri nhu bang, form, dialog, pagination, menu, notification... Rat phu hop voi he thong HRM vi loai bai toan nay co dac thu nhieu man hinh CRUD va nghiep vu quan ly noi bo.

#### Uu diem

- Tien do phat trien nhanh nho co san he thong component.
- Giao dien dong nhat va de chuan hoa.
- Ho tro tot cho dashboard, form validation, table va modal.

### Thu vien quan ly trang thai va dieu huong

- **Pinia**: quan ly state phia client.
- **Vue Router**: quan ly dinh tuyen va dieu huong trong SPA.

#### Uu diem

- **Pinia** nhe, don gian, phu hop Vue 3 va de to chuc state toan cuc nhu thong tin nguoi dung, token, bo loc du lieu.
- **Vue Router** cho phep tach cac man hinh nghiep vu thanh nhieu route ro rang, ho tro tot cho ung dung quan tri da module.

## 3. Cong nghe Back-end

Backend chinh cua he thong nam tai module **`Vantix`**, duoc quan ly phu thuoc bang Maven thong qua file `pom.xml`.

### Ngon ngu lap trinh

- **Java 17**

Java 17 la ban LTS (Long-Term Support), on dinh, hieu nang tot va duoc ho tro rong rai trong he sinh thai enterprise.

### Framework va cac starter chinh

- **Spring Boot**
- **spring-boot-starter-webmvc**
- **spring-boot-starter-data-jpa**
- **spring-boot-starter-security**
- **spring-boot-starter-validation**
- **spring-boot-starter-mail**
- **spring-boot-starter-websocket**

### Vai tro cua tung thanh phan

- **Spring Boot**: khung ung dung trung tam, giup cau hinh nhanh, giam boilerplate va de trien khai.
- **Spring Web MVC**: xay dung cac endpoint HTTP/API.
- **Spring Data JPA**: tang truy cap du lieu va ORM.
- **Spring Security**: xu ly xac thuc, phan quyen va bao ve tai nguyen.
- **Spring Validation**: kiem tra tinh hop le cua du lieu dau vao thong qua `@Valid`.
- **Spring Mail**: ho tro cac chuc nang gui email, dac biet cho quy trinh quen mat khau/OTP.
- **Spring WebSocket**: ho tro giao tiep thoi gian thuc, hien tai duoc su dung cho kenh thong bao `/ws/notifications`.

### Thu vien bo tro quan trong

- **Jackson Databind**: chuyen doi doi tuong Java thanh JSON va nguoc lai.
- **Lombok**: giam ma lap lai nhu getter, setter, constructor.
- **JJWT (io.jsonwebtoken)**: ho tro tao va xac thuc JSON Web Token.

### Hinh thuc dong goi va trien khai

- Du an duoc dong goi dang **WAR** (`<packaging>war</packaging>`).
- Co khai bao `spring-boot-starter-tomcat` voi scope `provided`, cho thay he thong co the duoc trien khai tren servlet container tuong thich Tomcat.

### Ly do lua chon

- **Spring Boot** rat phu hop cho he thong doanh nghiep vi he sinh thai day du, kha nang mo rong tot va cong dong lon.
- **Java 17 + Spring** dam bao tinh on dinh, bao tri lau dai va phu hop voi cac chuc nang nghiep vu phuc tap.
- **Security, Validation, Mail, WebSocket** giup du an tich hop day du cac nhu cau cot loi cua he thong HRM ma khong can xay dung lai tu dau.

## 4. Co che giao tiep giua Front-end va Back-end

Frontend va backend trao doi voi nhau chu yeu thong qua **RESTful API** su dung giao thuc **HTTP/HTTPS** va dinh dang du lieu **JSON**.

### Bang chung ky thuat

- Backend su dung nhieu `@RestController` voi namespace `/api/...` nhu:
  - `/api/auth`
  - `/api/employees`
  - `/api/contracts`
  - `/api/attendance`
  - `/api/payrolls`
  - `/api/notifications`
- Frontend cau hinh `axios.create({ baseURL: '/api' })`, cho thay toan bo request nghiep vu duoc gui den lop REST API.

### Thu vien goi API

- **Axios**

Frontend su dung `axios` de gui request HTTP. File cau hinh `src/api/http.js` cho thay:

- Cau hinh `baseURL: '/api'`
- Dat `Content-Type: 'application/json'`
- Dung **request interceptor** de gan header `Authorization: Bearer <token>`
- Dung **response interceptor** de xu ly loi `401`, `403`, `500`

### Co che xac thuc va bao mat giao tiep

- Backend su dung **JWT (JSON Web Token)** thong qua thu vien `jjwt`.
- Frontend luu token trong `localStorage` hoac `sessionStorage`, sau do gui kem token trong header `Authorization`.

Dieu nay cho thay he thong ap dung mo hinh xac thuc **token-based authentication**, phu hop voi SPA vi frontend va backend duoc tach rieng nhung van giu duoc co che bao mat thong nhat.

### Giao tiep thoi gian thuc

Ngoai REST API, he thong con ho tro **WebSocket** tai endpoint `/ws/notifications`, duoc proxy tu Vite sang backend.

#### Y nghia

- Ho tro day thong bao thoi gian thuc cho nguoi dung.
- Giam nhu cau polling lien tuc tu frontend.
- Nang cao trai nghiem nguoi dung doi voi cac chuc nang can cap nhat ngay lap tuc.

## 5. Co so du lieu va tang Data Access

### He quan tri co so du lieu

- **MySQL**

Can cu trong `application.properties`, he thong su dung chuoi ket noi:

- `jdbc:mysql://localhost:3306/vantix_db`

Dieu nay khang dinh CSDL chinh cua du an la **MySQL**, mot he quan tri quan he pho bien, phu hop voi cac he thong nghiep vu quan ly du lieu co cau truc ro rang nhu nhan vien, phong ban, hop dong, cham cong, bang luong va phan quyen.

### Ky thuat tuong tac voi CSDL

- **Spring Data JPA**
- **JPA/Hibernate ORM** (Hibernate la nha cung cap ORM mac dinh trong he sinh thai Spring Data JPA)

Tang truy cap du lieu duoc to chuc theo huong:

- Lop **Entity** su dung `@Entity`, `@Table` de anh xa bang du lieu.
- Lop **Repository** ke thua `JpaRepository<ENTITY, ID>`.
- Nghiep vu truy xuat va thao tac du lieu duoc dong goi trong cac service.

### Uu diem cua cach tiep can nay

- **Truu tuong hoa truy cap du lieu**: giam viec viet SQL thu cong cho cac thao tac CRUD thong thuong.
- **Tang nang suat phat trien**: co the sinh truy van tu ten method hoac mo rong tuy chinh khi can.
- **De bao tri**: ma nguon repository ngan gon, ro rang va nhat quan.
- **Gan ket tot voi mo hinh doi tuong**: Entity giup lien ket du lieu CSDL voi doi tuong nghiep vu mot cach tu nhien.

### Co che dong bo schema

- Cau hinh `spring.jpa.hibernate.ddl-auto=update`

Thiet lap nay cho phep Hibernate tu dong cap nhat cau truc bang theo entity trong giai doan phat trien, giup tang toc do lap trinh. Tuy nhien, trong moi truong san xuat, can can nhac su dung migration tool chuyen biet de dam bao kiem soat thay doi CSDL chat che hon.

## 6. Cong cu va moi truong phat trien

### He thong build va quan ly phu thuoc

- **Maven** cho backend Java/Spring Boot.
- **npm** cho frontend Vue/Vite.

#### Uu diem

- **Maven** quan ly dependency, vong doi build, test va dong goi WAR rat tot trong he Java.
- **npm** la cong cu chuan cho he sinh thai JavaScript, phu hop de quan ly package frontend.

### IDE/Moi truong phat trien

Trong thu muc goc repo co ton tai thu muc **`.idea`**, vi vay co the ket luan rang du an da duoc cau hinh hoac phat trien bang **IntelliJ IDEA** (hoac mot IDE JetBrains tuong thich).

#### Uu diem

- Ho tro rat tot cho Java, Spring Boot, Maven.
- Phan tich ma nguon, go loi, refactor va dieu huong code hieu qua.
- Phu hop voi du an full-stack co backend enterprise.

### Quan ly ma nguon

- **Git**

Su hien dien cua thu muc **`.git`** cho thay du an duoc quan ly bang Git.

#### Uu diem

- Quan ly lich su thay doi ma nguon.
- Ho tro lam viec nhom, branch va merge.
- Tao nen quy trinh phat trien chuyen nghiep va de truy vet thay doi.

### Cong cu test API

Trong pham vi hai file cau hinh `pom.xml`, `package.json` va cau truc repo hien tai, **chua thay cau hinh hoac tap tin dac thu** cho cac cong cu test API nhu Postman, Insomnia, Bruno hoac OpenAPI/Swagger.

Vi vay, ve mat hoc thuat va chinh xac ky thuat, co the trinh bay nhu sau:

- Du an **chua cung cap bang chung cau hinh truc tiep** ve cong cu test API trong repository hien tai.
- Qua kien truc REST API dang co, nhom phat trien **co the** su dung cac cong cu pho bien nhu **Postman** hoac **Swagger/OpenAPI** de kiem thu endpoint, nhung dieu nay **khong nen khang dinh nhu mot su that cau hinh** neu chua co tai lieu bo sung.

## 7. Danh gia tong hop ve lua chon cong nghe

Xet tong the, Vantix-Pro duoc xay dung tren nen tang cong nghe hien dai va phu hop voi mot he thong quan tri nhan su:

- **Vue 3 + Vite + Element Plus** giup frontend phat trien nhanh, giao dien quan tri dong bo va trai nghiem nguoi dung tot.
- **Spring Boot + Spring Security + Spring Data JPA** tao nen mot backend manh, de mo rong va phu hop voi nghiep vu doanh nghiep.
- **MySQL** dap ung tot nhu cau luu tru du lieu quan he co cau truc.
- **REST API + JSON + JWT** la bo ket hop phu hop cho kien truc tach biet FE-BE.
- **WebSocket** bo sung nang luc giao tiep thoi gian thuc, tang tinh tuong tac cua he thong.

Tu goc do kien truc he thong, day la mot lua chon cong nghe hop ly, can bang giua toc do phat trien, kha nang mo rong, tinh bao tri va muc do chuyen nghiep cua mot du an phan mem huong ung dung doanh nghiep.

## 8. Ket luan de dua vao bao cao

Co the ket luan rang Vantix-Pro la he thong duoc xay dung theo **kien truc Client-Server phan lop**, trong do frontend SPA duoc phat trien bang **Vue 3** va backend duoc xay dung bang **Spring Boot tren Java 17**. Hai thanh phan giao tiep chu yeu thong qua **RESTful API tra ve JSON**, co ket hop **JWT** cho xac thuc va **WebSocket** cho thong bao thoi gian thuc. Tang du lieu su dung **MySQL** ket hop **Spring Data JPA/Hibernate** de quan ly va truy cap CSDL. Tong the kien truc va cong nghe duoc lua chon the hien dinh huong xay dung mot he thong HRM hien dai, de mo rong, de bao tri va phu hop voi moi truong phat trien doanh nghiep.
