# vertx-adflex-structure

# Fix lỗi: Annotation processor 'io.vertx.codegen.CodeGenProcessor' not found
* Chỉnh sửa cấu hình trong file build.gradle
* Với gradle > 5 mở comment dòng: options.annotationProcessorPath = configurations.annotationProcessor 
* Với gradle < 5 comment dòng: options.annotationProcessorPath = configurations.annotationProcessor

#Chạy file: 
Set program argument: run vn.eway.MainVerticle -conf=<file_config>.json
Ex: run vn.eway.MainVerticle -conf=./src/main/java/conf/my_config.json
 * Lưu ý: trong file_config có cấu hình tới file xác thực: users.properties

