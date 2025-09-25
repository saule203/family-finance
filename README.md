# family-finance
一个简单的数据库课程设计系统 
⚙️ 项目配置说明

复制配置文件模板
在项目根目录下找到 src/main/resources/application-example.properties，复制一份并改名为：

cp src/main/resources/application-example.properties src/main/resources/application.properties


修改数据库连接信息
打开 application.properties，根据本地数据库情况修改以下配置：

spring.datasource.url=jdbc:mysql://localhost:3306/family_finance_db?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码


确认数据库存在
在 MySQL 中提前创建数据库（如果没有）：

CREATE DATABASE family_finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;


运行项目
使用 Maven Wrapper 或 IDE 启动 Spring Boot 应用：

./mvnw spring-boot:run


或者直接运行 FamilyfinanceApplication 主类。

验证运行
项目启动后，访问：

http://localhost:8080


即可看到项目运行情况。
