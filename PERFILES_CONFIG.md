# 🎯 CONFIGURACIÓN DE PERFILES - TODO POSTGRESQL

## ✅ ESTRUCTURA DE PERFILES

El proyecto usa **PostgreSQL en TODO** (local y producción):

### 📁 Archivos de Configuración:

```
src/main/resources/
├── application.properties          # Configuración común
├── application-local.properties    # Perfil LOCAL (PostgreSQL Local)
└── application-prod.properties     # Perfil PRODUCCIÓN (PostgreSQL Railway)
```

---

## 🏠 PERFIL LOCAL (PostgreSQL Local)

### application-local.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/p_agenda
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### ⚙️ Requisitos LOCAL:
1. **Instalar PostgreSQL** en tu máquina
2. **Crear base de datos**: `CREATE DATABASE p_agenda;`
3. **Usuario**: postgres
4. **Password**: postgres (o el que configures)

### ▶️ Cómo ejecutar en LOCAL:

**Opción 1 - IntelliJ/IDE:**
- El perfil `local` está activo por defecto
- Simplemente ejecuta la aplicación

**Opción 2 - Terminal:**
```bash
.\mvnw.cmd spring-boot:run
```

**Opción 3 - JAR:**
```bash
java -jar target/AgendarReservas-0.0.1-SNAPSHOT.jar
```

---

## 🚀 PERFIL PRODUCCIÓN (PostgreSQL - Railway)

### application-prod.properties
```properties
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/p_agenda}
spring.datasource.username=${PGUSER:postgres}
spring.datasource.password=${PGPASSWORD:}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=false
```

### ▶️ Cómo ejecutar en PRODUCCIÓN (Railway):

Railway usa automáticamente el perfil `prod` gracias a:
- `railway.json` configurado con `-Dspring.profiles.active=prod`
- `Procfile` configurado con `-Dspring.profiles.active=prod`

---

## 🔧 VARIABLES DE ENTORNO PARA RAILWAY

Configura estas variables en Railway:

```
DATABASE_URL = jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/p_agenda
PGUSER = ${{Postgres.PGUSER}}
PGPASSWORD = ${{Postgres.PGPASSWORD}}
ALLOWED_ORIGINS = http://localhost:5173
```

---

## 📦 DEPENDENCIAS (pom.xml)

Ahora SOLO PostgreSQL:

```xml
<!-- PostgreSQL ÚNICO driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

**NO MÁS MariaDB** - Todo es PostgreSQL ahora.

---

## 🎨 CONFIGURACIÓN COMÚN (application.properties)

```properties
spring.application.name=AgendarReservas
server.port=${PORT:8080}

# Perfil activo por defecto: local
spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}

# JPA/Hibernate (común a ambos perfiles)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# CORS
app.cors.allowed-origins=${ALLOWED_ORIGINS:http://localhost:5173}
```

---

## 🔄 CAMBIAR DE PERFIL MANUALMENTE

### En desarrollo local:

**Opción 1 - Variable de entorno:**
```bash
set SPRING_PROFILES_ACTIVE=local
.\mvnw.cmd spring-boot:run
```

**Opción 2 - Parámetro JVM:**
```bash
java -jar -Dspring.profiles.active=local target/AgendarReservas-0.0.1-SNAPSHOT.jar
```

**Opción 3 - IntelliJ:**
1. Run → Edit Configurations
2. Environment variables: `SPRING_PROFILES_ACTIVE=local`

### En Railway:
- El perfil `prod` se activa automáticamente (configurado en railway.json)

---

## ✅ VERIFICAR QUÉ PERFIL ESTÁ ACTIVO

Al iniciar la aplicación, busca en los logs:

```
The following 1 profile is active: "local"
```

o

```
The following 1 profile is active: "prod"
```

---

## 🎯 RESUMEN

| Entorno     | Perfil  | Base de Datos      | Driver      | Activación                |
|-------------|---------|--------------------| ------------|---------------------------|
| **Local**   | `local` | PostgreSQL Local   | PostgreSQL  | Por defecto               |
| **Railway** | `prod`  | PostgreSQL Railway | PostgreSQL  | Automático (railway.json) |

**TODO ES POSTGRESQL** - Sin excepciones.

---

## 🚀 PRÓXIMOS PASOS PARA RAILWAY

1. ✅ Código ya configurado con perfiles
2. ⏳ Hacer commit y push
3. ✅ Configurar variables de entorno en Railway
4. ✅ Crear base de datos `p_agenda`
5. ✅ Railway desplegará con perfil `prod` automáticamente

---

**Todo listo!** Ahora trabajas con **PostgreSQL en TODO** - local y producción. Sin MariaDB, sin complicaciones.

