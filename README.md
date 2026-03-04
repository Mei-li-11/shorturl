# ShortURL Service

一个基于Spring Boot的短链接服务，提供URL缩短、访问统计、用户认证等功能。

## 功能特点

- ✅ URL缩短：将长URL转换为短链接
- ✅ 访问统计：记录短链接的点击次数
- ✅ 用户认证：基于JWT的用户认证系统
- ✅ 速率限制：防止滥用API
- ✅ 过期链接清理：自动清理过期的短链接
- ✅ 数据初始化：系统启动时自动初始化测试数据
- ✅ OpenAPI支持：提供标准化的API接口
- ✅ API客户端认证：基于签名的API认证机制

## 技术栈

- **后端框架**：Spring Boot 4.0+
- **安全框架**：Spring Security
- **认证方式**：JWT (JSON Web Token)、API签名认证
- **数据库**：Spring Data JPA (支持多种数据库)
- **缓存**：Redis
- **构建工具**：Maven
- **Java版本**：Java 21+

## 项目结构

```
shorturl-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/shorturlservice/
│   │   │   ├── annotation/      # 自定义注解
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── interceptor/     # 拦截器
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── task/            # 定时任务
│   │   │   ├── test/            # 测试文件
│   │   │   ├── util/            # 工具类
│   │   │   └── ShorturlServiceApplication.java  # 应用入口
│   │   └── resources/
│   │       └── application.properties  # 配置文件
│   └── test/                    # 测试代码
├── pom.xml                      # Maven配置文件
├── mvnw                         # Maven wrapper
├── mvnw.cmd                     # Maven wrapper (Windows)
├── README.md                    # 项目说明
└── HELP.md                      # 帮助文档
```

## 快速开始

### 环境要求

- Java 21 或更高版本
- Maven 3.6 或更高版本
- 数据库（推荐使用MySQL）
- Redis（用于缓存和速率限制）

### 安装部署

1. **克隆项目**

   ```bash
   git clone https://github.com/Mei-li-11/shorturl.git
   cd shorturl-service
   ```

2. **配置数据库和Redis**

   修改 `src/main/resources/application.properties` 文件，配置数据库和Redis连接信息：

   ```properties
   # 应用名称
   spring.application.name=shorturl-service

   # 数据库配置
   spring.datasource.url=jdbc:mysql://localhost:3306/short_url_db?serverTimezone=Asia/Shanghai&characterEncoding=utf8
   spring.datasource.username=root
   spring.datasource.password=your-password

   # JPA配置
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true

   # Redis配置
   spring.data.redis.host=127.0.0.1
   spring.data.redis.port=6379

   # 服务器配置
   server.port=8080
   ```

3. **构建项目**

   ```bash
   ./mvnw clean package
   ```

4. **运行项目**

   ```bash
   java -jar target/shorturl-service-0.0.1-SNAPSHOT.jar
   ```

   或者使用Maven运行：

   ```bash
   ./mvnw spring-boot:run
   ```

### API接口

#### 认证接口

- **POST /api/auth/login** - 用户登录
  - 请求体：`{"username": "admin", "password": "password"}`
  - 响应：包含JWT token

#### 短链接接口

- **POST /api/short-url** - 创建短链接
  - 请求体：`{"originalUrl": "https://example.com/very/long/url"}`
  - 响应：包含短链接信息

- **GET /api/short-url** - 获取所有短链接
  - 需要JWT认证

- **GET /api/short-url/{id}** - 获取单个短链接
  - 需要JWT认证

- **DELETE /api/short-url/{id}** - 删除短链接
  - 需要JWT认证

#### OpenAPI接口

- **POST /openapi/short-url** - 创建短链接（API客户端认证）
  - 请求头：
    - `X-API-Key`: API客户端密钥
    - `X-Signature`: 请求签名
    - `X-Timestamp`: 时间戳
  - 请求体：`{"originalUrl": "https://example.com/very/long/url"}`
  - 响应：包含短链接信息

- **GET /openapi/short-url/{shortCode}** - 获取短链接信息（API客户端认证）
  - 请求头：
    - `X-API-Key`: API客户端密钥
    - `X-Signature`: 请求签名
    - `X-Timestamp`: 时间戳
  - 响应：包含短链接详细信息

#### 重定向接口

- **GET /{shortCode}** - 访问短链接，重定向到原始URL

## API客户端认证

### 认证流程

1. **获取API密钥**：通过系统管理员获取API客户端密钥
2. **生成签名**：使用API密钥对请求参数进行签名
3. **发送请求**：在请求头中包含API密钥、签名和时间戳
4. **服务端验证**：验证签名的有效性和时间戳的新鲜度

### 签名生成示例

```java
// 生成时间戳
long timestamp = System.currentTimeMillis();

// 构建签名字符串
StringBuilder signStr = new StringBuilder();
signStr.append("POST")
       .append("&")
       .append(URLEncoder.encode("/openapi/short-url", "UTF-8"))
       .append("&")
       .append(timestamp)
       .append("&")
       .append(URLEncoder.encode(requestBody, "UTF-8"));

// 使用API密钥进行HMAC-SHA256签名
String signature = HMACSHA256(signStr.toString(), apiSecret);
```

## 系统默认数据

系统启动时会自动初始化以下数据：

- **默认用户**：
  - 用户名：admin
  - 密码：password

- **默认短链接**：
  - 原始URL：https://www.example.com
  - 短链接：http://localhost:8080/abc123

- **默认API客户端**：
  - API Key：test-api-key
  - API Secret：test-api-secret

## 定时任务

- **点击计数同步**：每5分钟将内存中的点击计数同步到数据库
- **过期链接清理**：每天凌晨1点清理过期的短链接

## 速率限制

- 未认证请求：每分钟最多10次请求
- 已认证请求：每分钟最多30次请求
- API客户端请求：每分钟最多50次请求

## 安全配置

- 使用JWT进行身份认证
- 使用API签名进行API客户端认证
- 密码使用BCrypt加密存储
- 配置了CORS策略
- 启用了CSRF保护

## 开发指南

### 运行测试

```bash
./mvnw test
```

### 代码风格

- 遵循Spring Boot代码风格
- 使用Java 17+特性
- 保持代码简洁明了

## 许可证

本项目采用MIT许可证。详见LICENSE文件。

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

如有问题，请联系：
- GitHub: [Mei-li-11](https://github.com/Mei-li-11)
