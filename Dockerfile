# ==================== 阶段1：构建前端 ====================
FROM node:20-alpine AS frontend-builder

WORKDIR /build/mars-ui

# 先复制 package 文件，利用 Docker 缓存层加速依赖安装
COPY mars-ui/package.json mars-ui/package-lock.json* ./
RUN npm config set registry https://registry.npmmirror.com \
    && (npm ci --no-audit --no-fund || npm install --no-audit --no-fund)

# 复制前端源码并构建
# vite.config.ts 中 outDir 指向 ../mars-starter/src/main/resources/static
# 即构建产物输出到 /build/mars-starter/src/main/resources/static
COPY mars-ui/ ./
RUN npm run build


# ==================== 阶段2：构建后端 ====================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /build

# 配置阿里云 Maven 镜像源（解决国内服务器访问中央仓库 DNS/网络问题）
RUN mkdir -p /root/.m2 && cat > /root/.m2/settings.xml <<'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF

# 先复制所有 pom.xml，利用 Docker 缓存层加速依赖下载
COPY pom.xml ./
COPY mars-common/pom.xml mars-common/
COPY mars-infra/pom.xml mars-infra/
COPY mars-core/pom.xml mars-core/
COPY mars-api/pom.xml mars-api/
COPY mars-job/pom.xml mars-job/
COPY mars-starter/pom.xml mars-starter/

# 下载依赖（若 pom 未变动则走缓存）
RUN mvn dependency:go-offline -B -q || true

# 复制后端源码
COPY mars-common mars-common
COPY mars-infra mars-infra
COPY mars-core mars-core
COPY mars-api mars-api
COPY mars-job mars-job
COPY mars-starter mars-starter

# 将阶段1构建的前端静态资源覆盖到后端 static 目录
# （必须在 COPY mars-starter 之后执行，否则会被源码覆盖）
COPY --from=frontend-builder /build/mars-starter/src/main/resources/static ./mars-starter/src/main/resources/static

# 打包后端（跳过测试）
RUN mvn clean package -DskipTests -B -pl mars-starter -am


# ==================== 阶段3：运行时镜像 ====================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="mars-admin"

WORKDIR /app

# 安装时区数据与 curl（健康检查用）
RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 复制构建好的 jar
COPY --from=backend-builder /build/mars-starter/target/*.jar app.jar

# 创建文件上传目录（用于本地文件存储）
RUN mkdir -p /app/data/upload

EXPOSE 8080

# JVM 参数可通过 JAVA_OPTS 环境变量覆盖
ENV JAVA_OPTS="-Xms512m -Xmx512m -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
