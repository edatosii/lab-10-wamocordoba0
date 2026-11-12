package co.umariana.dao;

import co.umariana.conexion.ConexionBD;
import co.umariana.modelo.ArbolGenealogico;
import co.umariana.modelo.Persona;
import co.umariana.modelo.ResumenArbol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ArbolDAO {

    // ============================================================
    // ETAPA 4.1. GUARDAR UN ARBOL NUEVO
    // ============================================================

    public long guardar(String nombre, ArbolGenealogico arbol) {

        if (nombre == null || nombre.isBlank()
                || nombre.length() > 100 || arbol == null) {
            return -1;
        }

        String sql = "INSERT INTO arboles (nombre) VALUES (?)";

        try (Connection con = ConexionBD.conectar()) {

            try {
                con.setAutoCommit(false);

                long id;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, nombre);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se genero el id del arbol");
                        }

                        id = rs.getLong(1);
                    }
                }

                insertarMiembros(con, id, arbol);

                con.commit();

                return id;

            } catch (SQLException e) {

                System.out.println(e.getMessage());

                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }

                return -1;

            } finally {

                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return -1;
        }
    }

    // ============================================================
    // ETAPA 4.2. LISTAR ARBOLES
    // ============================================================

    public List<ResumenArbol> listar() {

        List<ResumenArbol> lista = new ArrayList<>();

        String sql = "SELECT a.id, a.nombre, a.creado, "
                + "COUNT(m.id) AS total "
                + "FROM arboles a "
                + "LEFT JOIN miembros m ON m.arbol_id = a.id "
                + "GROUP BY a.id, a.nombre, a.creado "
                + "ORDER BY a.id";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ResumenArbol resumen = new ResumenArbol(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getInt("total"),
                        rs.getTimestamp("creado").toLocalDateTime()
                );

                lista.add(resumen);
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return new ArrayList<>();
        }

        return lista;
    }

    // ============================================================
    // ETAPA 4.3. CARGAR UN ARBOL
    // ============================================================

    public ArbolGenealogico cargar(long id) {

        String sql = "SELECT posicion, nombre, nacimiento, "
                + "fallecimiento FROM miembros "
                + "WHERE arbol_id = ? ORDER BY id";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                ArbolGenealogico arbol = null;

                while (rs.next()) {

                    String posicion = rs.getString("posicion");
                    String nombre = rs.getString("nombre");
                    int nacimiento = rs.getInt("nacimiento");

                    int fallecimiento = rs.getInt("fallecimiento");

                    Integer anioFallecimiento =
                            rs.wasNull() ? null : fallecimiento;

                    Persona persona = new Persona(
                            posicion,
                            nombre,
                            nacimiento,
                            anioFallecimiento
                    );

                    if (arbol == null) {

                        arbol = new ArbolGenealogico(persona);

                    } else {

                        arbol.agregar(persona);
                    }
                }

                return arbol;
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return null;
        }
    }

    // ============================================================
    // ETAPA 4.4. ACTUALIZAR UN ARBOL
    // ============================================================

    public boolean actualizar(long id, ArbolGenealogico arbol) {

        if (arbol == null) {
            return false;
        }

        String sqlBuscar =
                "SELECT id FROM arboles WHERE id = ?";

        String sqlEliminar =
                "DELETE FROM miembros WHERE arbol_id = ?";

        try (Connection con = ConexionBD.conectar()) {

            try {
                con.setAutoCommit(false);

                try (PreparedStatement ps =
                        con.prepareStatement(sqlBuscar)) {

                    ps.setLong(1, id);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement ps =
                        con.prepareStatement(sqlEliminar)) {

                    ps.setLong(1, id);
                    ps.executeUpdate();
                }

                insertarMiembros(con, id, arbol);

                con.commit();

                return true;

            } catch (SQLException e) {

                System.out.println(e.getMessage());

                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }

                return false;

            } finally {

                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return false;
        }
    }

    // ============================================================
    // ETAPA 4.5. ELIMINAR UN ARBOL
    // ============================================================

    public boolean eliminar(long id) {

        String sql = "DELETE FROM arboles WHERE id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            int filas = ps.executeUpdate();

            return filas > 0;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
            return false;
        }
    }

    // ============================================================
    // METODO AUXILIAR PARA INSERTAR MIEMBROS
    // ============================================================

    private void insertarMiembros(
            Connection con,
            long id,
            ArbolGenealogico arbol) throws SQLException {

        String sql = "INSERT INTO miembros "
                + "(arbol_id, posicion, nombre, nacimiento, "
                + "fallecimiento) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (Persona persona : arbol.miembros()) {

                ps.setLong(1, id);
                ps.setString(2, persona.getPosicion());
                ps.setString(3, persona.getNombre());
                ps.setInt(4, persona.getNacimiento());

                if (persona.getFallecimiento() == null) {

                    ps.setNull(5, Types.INTEGER);

                } else {

                    ps.setInt(5, persona.getFallecimiento());
                }

                ps.executeUpdate();
            }
        }
    }
}