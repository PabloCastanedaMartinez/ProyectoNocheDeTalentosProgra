# demoInv — Gestor de Inventario (JavaFX + Gradle)

Aplicación de escritorio para gestionar un inventario simple: visualizar productos destacados, buscar, vender y registrar/ingresar productos. Está construida con **Java 21**, **JavaFX** y persistencia local en **JSON**.

## Requisitos

- **JDK 21** (el proyecto configura *toolchain* a Java 21 en `build.gradle.kts`).
- Conexión a internet la primera vez para descargar dependencias (Maven Central).
- No necesitas instalar Gradle: el proyecto incluye **Gradle Wrapper** (`gradlew.bat`).

## Tecnologías

- Java 21 (proyecto modular)
- JavaFX 21 (`javafx.controls`, `javafx.fxml`)
- Gson 2.11 (lectura/escritura de JSON)
- Gradle (Kotlin DSL)

## Cómo compilar

En Windows (PowerShell o CMD), desde la raíz del proyecto:

```powershell
gradlew clean build
```

Esto genera, entre otros:

- `build/libs/demoInv-1.0-SNAPSHOT.jar`
- Distribuciones en `build/distributions/`

## Cómo ejecutar

### Opción A: Ejecutar con Gradle

```powershell
gradlew run
```

### Opción B: Ejecutar el JAR

> Nota: al ser un proyecto JavaFX modular, la opción recomendada es `gradlew run`. Si tu entorno resuelve JavaFX en el classpath/módulos correctamente, podrás usar el JAR.

```powershell
java -jar .\build\libs\demoInv-1.0-SNAPSHOT.jar
```

## Cómo ejecutar desde un IDE

### Utilizando IntellijIDEA

1. Abre el proyecto en IntelliJ IDEA.
2. Asegúrate de que el SDK esté configurado a Java 21.
3. Busca la clase `org.cecade.demoinv.GestorInventario` en el panel de proyecto.
4. Presiona ejecutar (Run) en esa clase para iniciar la aplicación.

## ¿Cómo funciona?

### Punto de entrada

- La aplicación arranca en `org.cecade.demoinv.GestorInventario` (configurado en `build.gradle.kts`).
- `GestorInventario` extiende `javafx.application.Application` y construye la interfaz con JavaFX.

### Modelo

- `Producto` representa un ítem del inventario:
  - `id`
  - `nombre`
  - `precioUnitario` (BigDecimal)
  - `stock`
  - `ventasUltimos30Dias`
  - `rutaImagenLocal` (por ejemplo `images/laptop.png`)

### Persistencia (JSON)

- `ProductoRepository` gestiona lectura/escritura en `data/productos.json`.
- Al iniciar:
  - Si existe `data/productos.json`, lo carga con Gson.
  - Si no existe, crea una lista de productos precargados y guarda el JSON.
- Cada operación relevante (agregar/actualizar/stock) llama a `guardar()`.

### Lógica principal en pantalla

En el **Dashboard**:

- Se muestran “cards” de productos según criterio:
  - **Top Vendidos (últimos 30 días)** (`repo.topVendidos(3)`)
  - **Mayor Stock** (`repo.mayorStock(3)`)
- Acciones principales:
  - **Buscar Producto**: abre una pestaña de búsqueda.
  - **Vender**: descuenta stock del producto seleccionado y actualiza ventas (según implementación en `GestorInventario`).
  - **Ingresar Producto**: permite registrar un producto o incrementar stock (según implementación en `GestorInventario`).

### Imágenes y estilos

- Estilos CSS: `src/main/resources/org/cecade/demoinv/styles.css`
- Imágenes referenciadas por los productos: rutas tipo `images/*.png`
  - Se espera que estén bajo `src/main/resources/org/cecade/demoinv/images/`.
  - `ImageHelper` se encarga de cargar la imagen y devolver un placeholder si no existe.

## Datos: `data/productos.json`

- Ubicación: `data/productos.json` (relativo al directorio desde donde ejecutas la aplicación).
- Recomendación:
  - Evita editarlo mientras la aplicación está abierta.
  - Si se corrompe, puedes borrarlo para que se regenere con datos precargados al iniciar.

## Estructura del proyecto (resumen)

- `src/main/java/` código fuente
- `src/main/resources/` recursos (CSS e imágenes)
- `data/` datos persistidos (JSON)
- `build.gradle.kts` configuración de build/JavaFX

## Troubleshooting

- **No abre la ventana / error JavaFX**: usa `gradlew run` (maneja módulos y dependencias JavaFX correctamente).
- **No se ven imágenes**: verifica que existan los archivos `*.png` en `src/main/resources/org/cecade/demoinv/images/` con los mismos nombres que usa el JSON (p. ej. `laptop.png`).
- **El inventario “no guarda”**: comprueba permisos de escritura sobre la carpeta `data/`.

---

## Licencia

Este proyecto está publicado bajo **Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)**.

- Puedes **usar, copiar y modificar** el contenido **solo para fines no comerciales**.
- Debes **dar atribución**.
- **No se permite uso comercial / con fines de lucro**.

Consulta los términos completos en el archivo [`LICENSE`](./LICENSE).
