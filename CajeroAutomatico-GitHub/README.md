# Cajero Automático

Simulador de cajero automático para **retiros, consignaciones y consultas de saldo**, con registro de usuarios nuevos, inicio de sesión con tarjeta y PIN, comprobante de cada transacción e historial de movimientos.

Universidad Manuela Beltrán – Ingeniería de Software.

## 📱 Versión para celular

Abra este enlace desde el celular:

**https://TU-USUARIO.github.io/CajeroAutomatico/**

Para instalarla como app:
- **Android (Chrome):** menú ⋮ → *Instalar aplicación* o *Agregar a pantalla de inicio*.
- **iPhone (Safari):** botón Compartir → *Agregar a inicio*.

Después de instalada abre a pantalla completa y funciona sin internet. Los datos se guardan en el celular.

## 💻 Versión de escritorio (Java)

- **Doble clic** en `CajeroAutomatico.jar` (requiere Java 17 o superior), o en la terminal:
  ```
  java -jar CajeroAutomatico.jar
  ```
- **IntelliJ IDEA:** *File → Open*, seleccionar esta carpeta (proyecto Maven) y ejecutar `co.edu.umb.cajero.Main`.

## Cuentas de prueba

| Tarjeta | Titular | PIN | Saldo inicial |
|---|---|---|---|
| 4532 0151 1283 0366 | Ana María Pérez | 1234 | $1.500.000 |
| 5425 2334 3010 9903 | Carlos Rodríguez | 4321 | $350.000 |
| 4916 3385 0608 2832 | Laura Gómez | 1111 | $5.000.000 |

Cualquier persona puede crear su propia cuenta con el botón **¿No tiene cuenta? Crear cuenta nueva**.

## Reglas de negocio

- PIN de 4 dígitos; tras **3 intentos fallidos** la tarjeta se bloquea.
- Retiros en múltiplos de **$10.000**, máximo **$2.000.000** por transacción, sin superar el saldo.
- Consignaciones entre **$1.000** y **$10.000.000**.
- Registro: nombre solo con letras; PIN que no sea fácil (1111, 1234, 4321…); consignación inicial de $0 o entre $1.000 y $10.000.000.
- Cada operación queda registrada en el historial.

## Estructura del repositorio

```
├── index.html, manifest.webmanifest, sw.js, icon-*.png   Versión para celular (GitHub Pages)
├── CajeroAutomatico.jar                                   Ejecutable de escritorio
├── pom.xml                                                Proyecto Maven
├── src/main/java/co/edu/umb/cajero/
│   ├── Main.java          Punto de entrada
│   ├── modelo/            Cuenta, Movimiento, TipoMovimiento
│   ├── servicio/          CajeroService (lógica), RepositorioCuentas (archivos), OperacionException
│   └── vista/             VentanaCajero (pantallas), Estilo
├── src/test/java/...      20 pruebas unitarias (JUnit 5)
└── documento/             Documento técnico en Word
```

## Tecnologías

Java 17 · Swing · Maven · JUnit 5 · HTML, CSS y JavaScript (app web instalable)
