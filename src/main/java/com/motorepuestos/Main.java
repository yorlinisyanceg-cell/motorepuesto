package com.motorepuestos;

import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL = "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require";
    static final String USER = "postgres.zeozdkgetncpjmhircxg";
    static final String PASS = "motorespuesto";

    static Connection conn;
    static Scanner sc = new Scanner(System.in);

    // ─── utilidades de formato ────────────────────────────────────────────────

    static String rep(char c, int n) { StringBuilder sb = new StringBuilder(n); for (int i=0;i<n;i++) sb.append(c); return sb.toString(); }
    static void linea()  { System.out.println(rep('-', 72)); }
    static void linea2() { System.out.println(rep('=', 72)); }

    static String col(String s, int w) {
        if (s == null) s = "-";
        return s.length() > w ? s.substring(0, w - 1) + "…" : String.format("%-" + w + "s", s);
    }

    // ─── main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        linea2();
        System.out.println("          🔧  MOTO REPUESTOS  —  Sistema de Inventario");
        linea2();
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("  ✔ Conectado a Supabase\n");
            menu();
            conn.close();
        } catch (Exception e) {
            System.out.println("  ✘ Error de conexión: " + e.getMessage());
        }
    }

    // ─── menú ─────────────────────────────────────────────────────────────────

    static void menu() {
        while (true) {
            linea();
            System.out.println("  MENÚ PRINCIPAL");
            linea();
            System.out.println("  [1] Listar productos");
            System.out.println("  [2] Buscar producto");
            System.out.println("  [3] Agregar producto");
            System.out.println("  [4] Actualizar producto");
            System.out.println("  [5] Eliminar producto");
            System.out.println("  [6] Salir");
            linea();
            System.out.print("  Opción: ");

            String op = sc.nextLine().trim();
            System.out.println();

            if      (op.equals("1")) listar();
            else if (op.equals("2")) buscar();
            else if (op.equals("3")) agregar();
            else if (op.equals("4")) actualizar();
            else if (op.equals("5")) eliminar();
            else if (op.equals("6")) { System.out.println("  Hasta luego!\n"); return; }
            else System.out.println("  Opcion invalida");
        }
    }

    // ─── listar ───────────────────────────────────────────────────────────────

    static void listar() {
        try {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM producto ORDER BY id");

            encabezadoTabla();
            int n = 0;
            while (rs.next()) {
                filaProducto(rs);
                n++;
            }
            linea();
            System.out.printf("  Total: %d producto(s)%n", n);
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ─── buscar ───────────────────────────────────────────────────────────────

    static void buscar() {
        System.out.print("  Buscar (código/nombre/marca): ");
        String texto = sc.nextLine().trim();
        if (texto.isEmpty()) return;

        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM producto WHERE codigo ILIKE ? OR nombre ILIKE ? OR marca ILIKE ? ORDER BY id"
            );
            String q = "%" + texto + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);

            ResultSet rs = ps.executeQuery();
            encabezadoTabla();
            int n = 0;
            while (rs.next()) { filaProducto(rs); n++; }
            linea();
            System.out.printf("  %d resultado(s) para \"%s\"%n", n, texto);
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ─── tabla helpers ────────────────────────────────────────────────────────

    static void encabezadoTabla() {
        linea();
        System.out.printf("  %-4s %-10s %-20s %-10s %-12s %8s %8s %6s%n",
                "ID", "CODIGO", "NOMBRE", "TIPO", "CATEGORIA", "COSTO", "VENTA", "STOCK");
        linea();
    }

    static void filaProducto(ResultSet rs) throws SQLException {
        System.out.printf("  %-4d %-10s %-20s %-10s %-12s %8.2f %8.2f %6d%n",
                rs.getInt("id"),
                col(rs.getString("codigo"), 10),
                col(rs.getString("nombre"), 20),
                col(rs.getString("tipo_vehiculo"), 10),
                col(rs.getString("categoria"), 12),
                nullDouble(rs, "precio_costo"),
                nullDouble(rs, "precio_venta"),
                rs.getInt("stock"));
    }

    static double nullDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? 0 : v;
    }

    // ─── agregar ──────────────────────────────────────────────────────────────

    static void agregar() {
        System.out.println("  ── NUEVO PRODUCTO ──");
        try {
            System.out.print("  Código       : "); String codigo = sc.nextLine().trim();
            System.out.print("  Nombre       : "); String nombre = sc.nextLine().trim();
            System.out.print("  Descripción  : "); String desc   = sc.nextLine().trim();
            System.out.print("  Tipo (MOTO/BICICLETA): "); String tipo = sc.nextLine().trim().toUpperCase();
            System.out.print("  Categoría    : "); String cat    = sc.nextLine().trim();
            System.out.print("  Marca        : "); String marca  = sc.nextLine().trim();
            System.out.print("  Modelo       : "); String modelo = sc.nextLine().trim();
            System.out.print("  Año          : "); String anioStr = sc.nextLine().trim();
            System.out.print("  Proveedor    : "); String prov   = sc.nextLine().trim();
            System.out.print("  Precio costo : "); double costo  = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Precio venta : "); double venta  = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Stock inicial: "); int stock     = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO producto(codigo,nombre,descripcion,tipo_vehiculo,categoria,marca,modelo,anio,proveedor,precio_costo,precio_venta,stock) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
            );
            ps.setString(1, codigo);  ps.setString(2, nombre);  ps.setString(3, desc);
            ps.setString(4, tipo);    ps.setString(5, cat);      ps.setString(6, marca);
            ps.setString(7, modelo);
            if (anioStr.isEmpty()) ps.setNull(8, Types.INTEGER);
            else ps.setInt(8, Integer.parseInt(anioStr));
            ps.setString(9, prov);
            ps.setDouble(10, costo);  ps.setDouble(11, venta);   ps.setInt(12, stock);

            ps.executeUpdate();
            System.out.println("\n  ✔ Producto guardado correctamente!");
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Valor numérico inválido");
        } catch (Exception e) {
            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate"))
                System.out.println("  ✘ Ya existe un producto con ese código");
            else
                System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ─── actualizar ───────────────────────────────────────────────────────────

    static void actualizar() {
        try {
            System.out.print("  ID a editar: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            // mostrar producto actual
            PreparedStatement sel = conn.prepareStatement("SELECT * FROM producto WHERE id=?");
            sel.setInt(1, id);
            ResultSet rs = sel.executeQuery();
            if (!rs.next()) { System.out.println("  ✘ Producto no encontrado"); return; }

            System.out.println();
            System.out.printf("  Nombre actual  : %s%n", rs.getString("nombre"));
            System.out.printf("  Precio costo   : %.2f%n", nullDouble(rs, "precio_costo"));
            System.out.printf("  Precio venta   : %.2f%n", nullDouble(rs, "precio_venta"));
            System.out.printf("  Stock actual   : %d%n",   rs.getInt("stock"));
            System.out.println();

            System.out.print("  Nuevo precio venta (Enter para omitir): "); String sv = sc.nextLine().trim();
            System.out.print("  Nuevo stock        (Enter para omitir): "); String ss = sc.nextLine().trim();
            System.out.print("  Nuevo precio costo (Enter para omitir): "); String sc2 = sc.nextLine().trim();

            if (sv.isEmpty() && ss.isEmpty() && sc2.isEmpty()) {
                System.out.println("  Sin cambios."); return;
            }

            StringBuilder sql = new StringBuilder("UPDATE producto SET ");
            java.util.List<Object> params = new java.util.ArrayList<>();
            if (!sv.isEmpty())  { sql.append("precio_venta=?,");  params.add(Double.parseDouble(sv)); }
            if (!ss.isEmpty())  { sql.append("stock=?,");         params.add(Integer.parseInt(ss)); }
            if (!sc2.isEmpty()) { sql.append("precio_costo=?,");  params.add(Double.parseDouble(sc2)); }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(" WHERE id=?");
            params.add(id);

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ps.executeUpdate();
            System.out.println("  ✔ Producto actualizado!");
        } catch (NumberFormatException e) {
            System.out.println("  ✘ Valor numérico inválido");
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }

    // ─── eliminar ─────────────────────────────────────────────────────────────

    static void eliminar() {
        try {
            System.out.print("  ID a eliminar: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            // mostrar antes de eliminar
            PreparedStatement sel = conn.prepareStatement("SELECT nombre, codigo FROM producto WHERE id=?");
            sel.setInt(1, id);
            ResultSet rs = sel.executeQuery();
            if (!rs.next()) { System.out.println("  ✘ Producto no encontrado"); return; }
            System.out.printf("  ¿Eliminar \"%s\" (%s)? [s/N]: ", rs.getString("nombre"), rs.getString("codigo"));

            String conf = sc.nextLine().trim().toLowerCase();
            if (!conf.equals("s")) { System.out.println("  Cancelado."); return; }

            PreparedStatement ps = conn.prepareStatement("DELETE FROM producto WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("  ✔ Producto eliminado!");
        } catch (NumberFormatException e) {
            System.out.println("  ✘ ID inválido");
        } catch (Exception e) {
            System.out.println("  ✘ Error: " + e.getMessage());
        }
    }
}
