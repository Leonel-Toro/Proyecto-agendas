# Sistema de Autenticación JWT - Documentación

## 📋 Descripción General

Este sistema implementa autenticación completa basada en JWT (JSON Web Tokens) con las siguientes características:

- **Access Token**: Duración 1 hora (configurable)
- **Refresh Token**: Duración 7 días (configurable)
- **Almacenamiento seguro**: Cookies httpOnly
- **Protección CSRF**: Habilitada con token en cookie
- **Hasheado de contraseñas**: BCrypt con strength 12

## 🚀 Endpoints de Autenticación

### Públicos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/register` | Registrar nuevo usuario |
| POST | `/api/auth/refresh` | Refrescar access token |
| GET | `/api/auth/csrf` | Obtener token CSRF |

### Privados (requieren autenticación)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/logout` | Cerrar sesión |
| GET | `/api/auth/check-session` | Verificar sesión activa |

## 📝 Ejemplos de Uso

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "Admin123!"
  }' \
  -c cookies.txt
```

**Respuesta exitosa:**
```json
{
  "success": true,
  "message": "Inicio de sesión exitoso",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@agendarreservas.com",
    "roles": ["ROLE_ADMIN", "ROLE_USER"]
  }
}
```

### Registro

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nuevoUsuario",
    "email": "usuario@ejemplo.com",
    "password": "MiPassword123!"
  }'
```

### Check Session

```bash
curl -X GET http://localhost:8080/api/auth/check-session \
  -b cookies.txt
```

### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -b cookies.txt
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -b cookies.txt \
  -c cookies.txt
```

## ⚙️ Configuración

### Variables de Entorno (Producción)

```properties
# JWT
JWT_SECRET=<base64-encoded-512bit-key>
JWT_ACCESS_EXPIRATION=3600000      # 1 hora en ms
JWT_REFRESH_EXPIRATION=604800000   # 7 días en ms
JWT_ISSUER=AgendarReservas

# Cookies
COOKIE_SECURE=true
COOKIE_SAME_SITE=Strict
COOKIE_DOMAIN=tudominio.com

# CSRF
CSRF_ENABLED=true

# Admin inicial
APP_ADMIN_USERNAME=admin
APP_ADMIN_EMAIL=admin@tudominio.com
APP_ADMIN_PASSWORD=TuPasswordSeguro123!
APP_ADMIN_CREATE_ON_STARTUP=true
```

### Generar Secret Key Seguro

```bash
# Linux/Mac
openssl rand -base64 64

# PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### 🔑 Gestión Automática de JWT_SECRET

El sistema tiene gestión inteligente del JWT_SECRET:

#### Prioridad de configuración:
1. **Variable de entorno `JWT_SECRET`** (recomendado para producción)
2. **Archivo de propiedades** (`application.properties`)
3. **Archivo local `.jwt-secret`** (auto-generado para desarrollo)

#### Comportamiento por entorno:

| Entorno | Sin JWT_SECRET configurado | Acción |
|---------|----------------------------|--------|
| **Desarrollo** | Genera automáticamente y guarda en `.jwt-secret` | ⚠️ Advertencia en logs |
| **Producción** | **Error fatal** - La app no inicia | 🔴 Debe configurar manualmente |

#### Logs que verás:

```
# Desarrollo - primera ejecución:
🔑 Generado nuevo JWT_SECRET y guardado en: .jwt-secret
   Este archivo está excluido de Git (.gitignore)

# Desarrollo - ejecuciones siguientes:
📁 Usando JWT_SECRET del archivo local: .jwt-secret

# Producción - correctamente configurado:
✅ JWT_SECRET configurado via variable de entorno
✅ JWT_SECRET tiene fortaleza adecuada (64 bytes)

# Producción - sin configurar:
🔴 CRÍTICO: No se ha configurado JWT_SECRET para producción!
   Configure la variable de entorno JWT_SECRET con una clave segura
```

#### ¿Por qué este diseño?

- **Desarrollo fácil**: No necesitas configurar nada, funciona automáticamente
- **Producción segura**: Te obliga a configurar un secret fuerte
- **Persistencia local**: El archivo `.jwt-secret` persiste entre reinicios
- **Excluido de Git**: Nunca se sube al repositorio

## 🍪 Cookies

El sistema usa dos cookies httpOnly:

| Cookie | Path | Duración | Descripción |
|--------|------|----------|-------------|
| `access_token` | `/` | 1 hora | JWT para autenticación |
| `refresh_token` | `/api/auth` | 7 días | Token para renovar access token |

### Atributos de Seguridad

- **HttpOnly**: Previene acceso desde JavaScript (protección XSS)
- **Secure**: Solo enviadas sobre HTTPS (en producción)
- **SameSite**: Strict en producción (protección CSRF adicional)

## 🔐 Seguridad

### CSRF Protection

Para endpoints que modifican datos (POST, PUT, DELETE):

1. Obtener token CSRF: `GET /api/auth/csrf`
2. Incluir header: `X-XSRF-TOKEN: <token>`

**Nota**: El login y register están exentos de CSRF.

### Headers de Seguridad

El sistema configura automáticamente:

- Content-Security-Policy
- X-Frame-Options: SAMEORIGIN
- X-XSS-Protection
- X-Content-Type-Options: nosniff
- Strict-Transport-Security (HSTS)

## 📊 Base de Datos

### Tablas Creadas

1. **users**: Almacena usuarios
2. **user_roles**: Roles de cada usuario
3. **refresh_tokens**: Tokens de refresco activos

### Roles Disponibles

- `ROLE_USER`: Usuario estándar
- `ROLE_ADMIN`: Administrador

## 🧪 Usuario Admin Por Defecto

Al iniciar la aplicación se crea automáticamente:

- **Username**: admin
- **Email**: admin@agendarreservas.com
- **Password**: Admin123!
- **Roles**: ROLE_ADMIN, ROLE_USER

⚠️ **IMPORTANTE**: Cambiar la contraseña en producción.

## 📁 Estructura de Archivos

```
src/main/java/com/Calendario/AgendarReservas/
├── Config/
│   └── DataInitializer.java          # Crea usuario admin inicial
├── Controller/
│   └── AuthController.java           # Endpoints de autenticación
├── DTO/
│   ├── AuthResponse.java             # Respuesta de autenticación
│   ├── LoginRequest.java             # Request de login
│   └── RegisterRequest.java          # Request de registro
├── Exception/
│   ├── GlobalExceptionHandler.java   # Manejo global de errores
│   ├── TokenRefreshException.java    # Error de refresh token
│   └── UserAlreadyExistsException.java
├── Model/
│   ├── RefreshToken.java             # Entidad refresh token
│   ├── Role.java                     # Enum de roles
│   └── User.java                     # Entidad usuario
├── Repository/
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── Security/
│   ├── CustomAccessDeniedHandler.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java  # Filtro JWT
│   ├── SecurityConfig.java           # Configuración Spring Security
│   └── WebConfig.java                # Configuración CORS
└── Service/
    ├── AuthService.java              # Lógica de autenticación
    ├── CustomUserDetailsService.java
    ├── JwtService.java               # Generación/validación JWT
    └── RefreshTokenService.java      # Gestión refresh tokens
```

## 🔄 Flujo de Autenticación

```
┌─────────────┐     1. POST /login      ┌─────────────┐
│   Cliente   │ ───────────────────────>│   Backend   │
│   (React)   │                         │   (Spring)  │
│             │<─────────────────────── │             │
└─────────────┘  2. Set-Cookie:         └─────────────┘
                    access_token
                    refresh_token
       │
       │ 3. Request con cookies
       ▼
┌─────────────┐                         ┌─────────────┐
│   Cliente   │ ───────────────────────>│   Backend   │
│             │  Cookie: access_token   │             │
│             │<─────────────────────── │             │
└─────────────┘  4. Respuesta           └─────────────┘

       │
       │ 5. Token expirado
       ▼
┌─────────────┐  POST /refresh          ┌─────────────┐
│   Cliente   │ ───────────────────────>│   Backend   │
│             │  Cookie: refresh_token  │             │
│             │<─────────────────────── │             │
└─────────────┘  6. Nuevas cookies      └─────────────┘
```

## 🌐 Configuración Frontend (React)

```javascript
// Configuración de fetch
const apiCall = async (url, options = {}) => {
  const response = await fetch(url, {
    ...options,
    credentials: 'include', // ¡IMPORTANTE para enviar cookies!
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
  
  // Si recibimos 401, intentar refresh
  if (response.status === 401) {
    const refreshResponse = await fetch('/api/auth/refresh', {
      method: 'POST',
      credentials: 'include',
    });
    
    if (refreshResponse.ok) {
      // Reintentar la petición original
      return fetch(url, { ...options, credentials: 'include' });
    } else {
      // Redirigir a login
      window.location.href = '/login';
    }
  }
  
  return response;
};
```

## ✅ Checklist de Producción

- [ ] Generar nuevo JWT_SECRET (64 bytes, base64)
- [ ] Configurar COOKIE_SECURE=true
- [ ] Configurar COOKIE_SAME_SITE=Strict
- [ ] Cambiar contraseña del admin
- [ ] Habilitar HTTPS
- [ ] Configurar CORS con dominio exacto
- [ ] Revisar variables de entorno
- [ ] Deshabilitar creación automática de admin (opcional)

---

## 📅 Endpoints de Reservas

Todas las reservas están asociadas al usuario autenticado. Un usuario solo puede ver y modificar sus propias reservas.

### Endpoints de Usuario (requieren autenticación)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/reservas/agendar` | Crear nueva reserva (asignada al usuario actual) |
| GET | `/api/reservas/historial` | Obtener historial de reservas del usuario |
| GET | `/api/reservas/historial/detalle/{id}` | Obtener detalle de una reserva propia |
| PUT | `/api/reservas/editar` | Editar una reserva propia |
| DELETE | `/api/reservas/eliminar/{id}` | Eliminar una reserva propia |

### Endpoints de Admin (requieren rol ADMIN)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reservas/admin/historial/usuario/{userId}` | Ver reservas de cualquier usuario |
| GET | `/api/reservas/admin/detalle/{id}` | Ver detalle de cualquier reserva |
| PUT | `/api/reservas/admin/editar` | Editar cualquier reserva |

### Modelo de Datos

La entidad `Reserva` ahora incluye:
- **user_id**: FK a la tabla `users` - propietario de la reserva
- **id_cliente**: FK a la tabla `cliente` - cliente de la reserva

### Seguridad de Reservas

```
┌─────────────────────────────────────────────────────────────┐
│ Usuario "juan" hace: GET /api/reservas/historial            │
├─────────────────────────────────────────────────────────────┤
│ 1. JwtAuthenticationFilter valida el token                  │
│ 2. SecurityContext contiene la identidad de "juan"          │
│ 3. ReservaService.getCurrentUser() obtiene el User          │
│ 4. Query: SELECT * FROM reserva WHERE user_id = juan.id     │
│ 5. Solo devuelve las reservas de "juan"                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ Usuario "juan" hace: GET /api/reservas/historial/detalle/5  │
│ (donde la reserva 5 pertenece a "pedro")                    │
├─────────────────────────────────────────────────────────────┤
│ 1. JwtAuthenticationFilter valida el token                  │
│ 2. ReservaService busca: findByIdReservaAndUserId(5, juan)  │
│ 3. No encuentra la reserva → Devuelve 404                   │
│ 4. Juan NO puede ver las reservas de Pedro                  │
└─────────────────────────────────────────────────────────────┘
```

