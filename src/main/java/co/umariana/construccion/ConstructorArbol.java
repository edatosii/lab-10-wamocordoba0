package co.umariana.construccion;

import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.ArbolInvalidoException;
import co.umariana.modelo.Persona;
import co.umariana.modelo.Posicion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Construye un árbol genealógico a partir de un texto: cada línea es una persona.
 *
 *     posicion;nombre;nacimiento;fallecimiento
 *
 * Ejemplo (el fallecimiento puede quedar vacío):
 *
 *     1;John Doe;2002;
 *     2;Henry Doe;1920;1990
 *     3;Maria Doe;1921;
 *     1.1;Timmy Doe;2021;
 *     1.2;Amy Doe;2026;
 *
 * INSTRUCCIONES PARA EL ESTUDIANTE (Etapa 3)
 * ------------------------------------------
 * Reemplace el "throw new UnsupportedOperationException" por su implementación. Esta clase usa lo que ya
 * hizo en las etapas anteriores (Posicion, Persona y ArbolGenealogico). NO modifique las pruebas (src/test).
 *
 * REGLAS
 *   1. Si el texto es null o no tiene ninguna línea con contenido: ArbolInvalidoException (mensaje: "El texto está vacío...").
 *   2. Las líneas pueden terminar en \n o en \r\n. Las líneas en blanco se ignoran, pero los números de línea
 *      que se informan cuentan TODAS las líneas del texto (la primera es la línea 1).
 *   3. Cada línea tiene 4 campos separados por ';' (el cuarto, el fallecimiento, puede estar vacío). Se acepta
 *      también la línea con solo 3 campos y sin el último ';'. Los campos se recortan (trim).
 *      Si hay menos de 3 o más de 4 campos: error.
 *   4. El nacimiento y el fallecimiento deben ser números enteros (el fallecimiento vacío es "no tiene").
 *   5. Cada persona debe ser válida (Persona.validar) y no puede haber dos líneas con la misma posición.
 *   6. Debe existir la persona "1" (el sujeto), pero puede estar en cualquier línea.
 *   7. Las líneas pueden venir en CUALQUIER orden. El constructor debe agregarlas al árbol en un orden en el
 *      que cada persona ya tenga a la que necesita (por ejemplo, primero los ascendientes por número Ahnentafel
 *      ascendente y los descendientes por generación). Las reglas del árbol (ver ArbolGenealogico) las valida el árbol.
 *   8. Todos los errores asociados a una línea se informan con ArbolInvalidoException y un mensaje que EMPIEZA con
 *      "Línea N: " (N es el número de línea de la persona con el problema), por ejemplo:
 *          "Línea 3: la posición 2 está repetida."
 *      Los errores que no son de una línea (texto vacío, falta el sujeto) no llevan ese prefijo.
 *   9. Solo se muestran los miembros que están en el texto: no se crean personas vacías para los que faltan.
 */
public final class ConstructorArbol {

    private ConstructorArbol() {
    }

    // Clase auxiliar para conservar la persona y su número de línea.
    private static class Registro {

        Persona persona;
        int numeroLinea;

        Registro(Persona persona, int numeroLinea) {
            this.persona = persona;
            this.numeroLinea = numeroLinea;
        }
    }

    /**
     * Convierte el texto en un árbol (ver las reglas arriba).
     *
     * @throws ArbolInvalidoException si el texto no cumple las reglas
     */
    public static ArbolGenealogico construir(String texto) {
        // TODO Etapa 3

        // Validar que el texto no sea nulo ni esté vacío.
        if (texto == null || texto.isBlank()) {
            throw new ArbolInvalidoException(
                    "El texto está vacío o no contiene personas");
        }

        List<Registro> registros = new ArrayList<>();
        Registro sujeto = null;

        // Separar las líneas conservando las líneas vacías.
        String[] lineas = texto.split("\\r?\\n", -1);

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i].trim();
            int numeroLinea = i + 1;

            // Ignorar las líneas en blanco.
            if (linea.isEmpty()) {
                continue;
            }

            // Separar los campos conservando los valores vacíos.
            String[] campos = linea.split(";", -1);

            // Verificar que tenga tres o cuatro campos.
            if (campos.length < 3 || campos.length > 4) {
                throw new ArbolInvalidoException(
                        "Línea " + numeroLinea
                        + ": cantidad de campos incorrecta");
            }

            String posicion = campos[0].trim();
            String nombre = campos[1].trim();
            String textoNacimiento = campos[2].trim();

            String textoFallecimiento = "";

            if (campos.length == 4) {
                textoFallecimiento = campos[3].trim();
            }

            int nacimiento;
            Integer fallecimiento = null;

            // Convertir el año de nacimiento a entero.
            try {
                nacimiento = Integer.parseInt(textoNacimiento);

            } catch (NumberFormatException e) {
                throw new ArbolInvalidoException(
                        "Línea " + numeroLinea
                        + ": el año de nacimiento no es válido");
            }

            // Convertir el año de fallecimiento cuando exista.
            if (!textoFallecimiento.isEmpty()) {

                try {
                    fallecimiento =
                            Integer.parseInt(textoFallecimiento);

                } catch (NumberFormatException e) {
                    throw new ArbolInvalidoException(
                            "Línea " + numeroLinea
                            + ": el año de fallecimiento no es válido");
                }
            }

            // Crear la persona con los datos de la línea.
            Persona persona = new Persona(
                    posicion,
                    nombre,
                    nacimiento,
                    fallecimiento
            );

            // Validar la información de la persona.
            try {
                persona.validar();

            } catch (IllegalArgumentException e) {
                throw new ArbolInvalidoException(
                        "Línea " + numeroLinea
                        + ": " + e.getMessage());
            }

            // Verificar que la posición no esté repetida.
            for (Registro registro : registros) {

                if (registro.persona.getPosicion()
                        .equals(persona.getPosicion())) {

                    throw new ArbolInvalidoException(
                            "Línea " + numeroLinea
                            + ": la posición " + posicion
                            + " está repetida");
                }
            }

            // Guardar la persona y el número de línea.
            Registro registro = new Registro(
                    persona, numeroLinea);

            registros.add(registro);

            // Identificar al sujeto del árbol.
            if (Posicion.esSujeto(posicion)) {
                sujeto = registro;
            }
        }

        // Verificar que el texto tenga al menos una persona.
        if (registros.isEmpty()) {
            throw new ArbolInvalidoException(
                    "El texto está vacío o no contiene personas");
        }

        // Verificar que exista el sujeto de posición 1.
        if (sujeto == null) {
            throw new ArbolInvalidoException(
                    "Falta el sujeto de posición 1");
        }

        // Crear el árbol genealógico con el sujeto.
        ArbolGenealogico arbol;

        try {
            arbol = new ArbolGenealogico(sujeto.persona);

        } catch (IllegalArgumentException e) {
            throw new ArbolInvalidoException(
                    "Línea " + sujeto.numeroLinea
                    + ": " + e.getMessage());
        }

        // Ordenar las personas para respetar las dependencias.
        registros.sort(new Comparator<Registro>() {

            @Override
            public int compare(Registro r1, Registro r2) {

                String p1 = r1.persona.getPosicion();
                String p2 = r2.persona.getPosicion();

                // El sujeto debe ir primero.
                if (Posicion.esSujeto(p1)) {
                    return -1;
                }

                if (Posicion.esSujeto(p2)) {
                    return 1;
                }

                boolean a1 = Posicion.esAscendiente(p1);
                boolean a2 = Posicion.esAscendiente(p2);

                // Los ascendientes van antes de los descendientes.
                if (a1 && !a2) {
                    return -1;
                }

                if (!a1 && a2) {
                    return 1;
                }

                // Ordenar ascendientes por número Ahnentafel.
                if (a1 && a2) {
                    return Integer.compare(
                            Integer.parseInt(p1),
                            Integer.parseInt(p2));
                }

                // Ordenar descendientes por generación.
                int g1 = Posicion.generacion(p1);
                int g2 = Posicion.generacion(p2);

                if (g1 != g2) {
                    return Integer.compare(g1, g2);
                }

                // Conservar el orden original en igual generación.
                return Integer.compare(
                        r1.numeroLinea,
                        r2.numeroLinea);
            }
        });

        // Agregar las personas al árbol genealógico.
        for (Registro registro : registros) {

            Persona persona = registro.persona;

            // El sujeto ya fue agregado en el constructor.
            if (Posicion.esSujeto(persona.getPosicion())) {
                continue;
            }

            try {
                arbol.agregar(persona);

            } catch (IllegalArgumentException e) {
                throw new ArbolInvalidoException(
                        "Línea " + registro.numeroLinea
                        + ": " + e.getMessage());
            }
        }

        // Devolver el árbol construido.
        return arbol;
    }
}