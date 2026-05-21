package com.motorepuestos;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final JdbcTemplate db;

    public ProductoService(JdbcTemplate db) { this.db = db; }

    private final RowMapper<Producto> mapper = (rs, i) -> {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setTipoVehiculo(rs.getString("tipo_vehiculo"));
        p.setCategoria(rs.getString("categoria"));
        p.setMarca(rs.getString("marca"));
        p.setModelo(rs.getString("modelo"));
        p.setAnio((Integer) rs.getObject("anio"));
        p.setProveedor(rs.getString("proveedor"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioCosto(rs.getObject("precio_costo") != null ? rs.getDouble("precio_costo") : null);
        p.setPrecioVenta(rs.getObject("precio_venta") != null ? rs.getDouble("precio_venta") : null);
        p.setStock(rs.getInt("stock"));
        return p;
    };

    public List<Producto> listar(String filtro) {
        if (filtro == null || filtro.isBlank())
            return db.query("SELECT * FROM producto ORDER BY id", mapper);
        String q = "%" + filtro.toLowerCase() + "%";
        return db.query(
            "SELECT * FROM producto WHERE LOWER(codigo) LIKE ? OR LOWER(nombre) LIKE ? OR LOWER(marca) LIKE ? ORDER BY id",
            mapper, q, q, q);
    }

    public void guardar(Producto p) {
        if (p.getId() == null) {
            db.update("INSERT INTO producto(codigo,nombre,descripcion,tipo_vehiculo,categoria,marca,modelo,anio,proveedor,precio_costo,precio_venta,stock) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getTipoVehiculo(), p.getCategoria(),
                p.getMarca(), p.getModelo(), p.getAnio(), p.getProveedor(),
                p.getPrecioCosto(), p.getPrecioVenta(), p.getStock());
        } else {
            db.update("UPDATE producto SET codigo=?,nombre=?,descripcion=?,tipo_vehiculo=?,categoria=?,marca=?,modelo=?,anio=?,proveedor=?,precio_costo=?,precio_venta=?,stock=? WHERE id=?",
                p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getTipoVehiculo(), p.getCategoria(),
                p.getMarca(), p.getModelo(), p.getAnio(), p.getProveedor(),
                p.getPrecioCosto(), p.getPrecioVenta(), p.getStock(), p.getId());
        }
    }

    public void eliminar(Integer id) {
        db.update("DELETE FROM producto WHERE id=?", id);
    }
}
