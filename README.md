# Trabajo de Ficheros para la asignatura Acceso a Datos #
### Por Daniel Acedo Calderón ###

## Descripción ##

La aplicación permite descargar un fichero conteniendo una serie de enlaces a imágenes y otro conteniendo frases. Cuando los descarga muestra automáticamente cada cierto tiempo el siguiente de su galería. El tiempo que se tarda en pasar a la siguiente posición esta almacenado en el archivo raw/intervalo.txt, notado en segundos.

Las imágenes se cargan usando la librería Picasso. Mientras se están cargando las imágenes se muestra un icono de carga y en el caso de que no se pueda cargar se muestra una imagen de error.

Las peticiones se realizan usando AsyncHttpClient y la clase RestClient.

Se comprueba que los enlaces estén bien formados para no lanzar excepciones al intentar enviar peticiones. En el caso de que el enlace este vacío o no sea válido no se bajará. Cada galería es independiente de la otra, así que si una falla al descargarse la otra se inicializará por su cuenta. 

Debajo de cada elemento (imagen o frase) se mostrará la posición del elemento en la galería y el número de elementos que tiene la galería.

En el caso de que haya un error al bajar alguno de los archivos o al cargar una imagen, se enviará el error junto con la fecha y hora actual a un script php alojado en un servidor que añadirá el error a un archivo.
