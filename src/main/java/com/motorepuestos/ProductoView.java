package com.motorepuestos;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Moto Repuestos")
public class ProductoView extends VerticalLayout {

    private final ProductoService service;
    private final Grid<Producto> grid = new Grid<>(Producto.class, false);
    private final Binder<Producto> binder = new Binder<>(Producto.class);
    private Producto productoActual;

    // campos del formulario
    private final TextField codigo      = new TextField("Codigo");
    private final TextField nombre      = new TextField("Nombre");
    private final TextField tipo        = new TextField("Tipo (MOTO/BICICLETA)");
    private final TextField categoria   = new TextField("Categoria");
    private final TextField marca       = new TextField("Marca");
    private final TextField modelo      = new TextField("Modelo");
    private final IntegerField anio     = new IntegerField("Anio");
    private final TextField proveedor   = new TextField("Proveedor");
    private final TextField descripcion = new TextField("Descripcion");
    private final NumberField costo     = new NumberField("Precio Costo");
    private final NumberField venta     = new NumberField("Precio Venta");
    private final IntegerField stock    = new IntegerField("Stock");

    private final TextField busqueda = new TextField();

    public ProductoView(ProductoService service) {
        this.service = service;
        setSizeFull();
        setPadding(false);

        configurarGrid();
        configurarBinder();

        SplitLayout split = new SplitLayout(crearListado(), crearFormulario());
        split.setSizeFull();
        split.setSplitterPosition(65);

        add(split);
        refrescar();
    }

    private VerticalLayout crearListado() {
        busqueda.setPlaceholder("Buscar por codigo, nombre o marca...");
        busqueda.setWidth("300px");
        busqueda.addValueChangeListener(e -> refrescar());

        Button nuevo = new Button("+ Nuevo", e -> editarProducto(new Producto()));
        nuevo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(busqueda, nuevo);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setPadding(true);

        VerticalLayout layout = new VerticalLayout(new H2("Inventario"), toolbar, grid);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private VerticalLayout crearFormulario() {
        FormLayout form = new FormLayout(codigo, nombre, tipo, categoria, marca, modelo, anio, proveedor, descripcion, costo, venta, stock);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        Button guardar = new Button("Guardar", e -> guardar());
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> limpiarFormulario());

        Button eliminar = new Button("Eliminar", e -> confirmarEliminar());
        eliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout botones = new HorizontalLayout(guardar, cancelar, eliminar);

        VerticalLayout layout = new VerticalLayout(new H2("Detalle"), form, botones);
        layout.setPadding(true);
        return layout;
    }

    private void configurarGrid() {
        grid.addColumn(Producto::getCodigo).setHeader("Codigo").setSortable(true);
        grid.addColumn(Producto::getNombre).setHeader("Nombre").setSortable(true);
        grid.addColumn(Producto::getTipoVehiculo).setHeader("Tipo");
        grid.addColumn(Producto::getCategoria).setHeader("Categoria");
        grid.addColumn(Producto::getMarca).setHeader("Marca");
        grid.addColumn(p -> p.getPrecioVenta() != null ? String.format("$%.2f", p.getPrecioVenta()) : "-").setHeader("Precio Venta");
        grid.addColumn(Producto::getStock).setHeader("Stock").setSortable(true);
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) editarProducto(e.getValue());
        });
    }

    private void configurarBinder() {
        binder.bind(codigo,      Producto::getCodigo,       Producto::setCodigo);
        binder.bind(nombre,      Producto::getNombre,       Producto::setNombre);
        binder.bind(tipo,        Producto::getTipoVehiculo, Producto::setTipoVehiculo);
        binder.bind(categoria,   Producto::getCategoria,    Producto::setCategoria);
        binder.bind(marca,       Producto::getMarca,        Producto::setMarca);
        binder.bind(modelo,      Producto::getModelo,       Producto::setModelo);
        binder.bind(anio,        Producto::getAnio,         Producto::setAnio);
        binder.bind(proveedor,   Producto::getProveedor,    Producto::setProveedor);
        binder.bind(descripcion, Producto::getDescripcion,  Producto::setDescripcion);
        binder.bind(costo,       Producto::getPrecioCosto,  Producto::setPrecioCosto);
        binder.bind(venta,       Producto::getPrecioVenta,  Producto::setPrecioVenta);
        binder.bind(stock,       Producto::getStock,        Producto::setStock);
    }

    private void editarProducto(Producto p) {
        productoActual = p;
        binder.readBean(p);
    }

    private void guardar() {
        if (productoActual == null) return;
        try {
            binder.writeBean(productoActual);
            service.guardar(productoActual);
            refrescar();
            limpiarFormulario();
            notificar("Guardado correctamente", false);
        } catch (Exception e) {
            notificar("Error: " + e.getMessage(), true);
        }
    }

    private void confirmarEliminar() {
        if (productoActual == null || productoActual.getId() == null) return;
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar producto");
        dialog.setText("¿Eliminar \"" + productoActual.getNombre() + "\"?");
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText("Cancelar");
        dialog.setCancelable(true);
        dialog.addConfirmListener(e -> {
            service.eliminar(productoActual.getId());
            refrescar();
            limpiarFormulario();
            notificar("Producto eliminado", false);
        });
        dialog.open();
    }

    private void limpiarFormulario() {
        productoActual = null;
        binder.readBean(new Producto());
        grid.asSingleSelect().clear();
    }

    private void refrescar() {
        grid.setItems(service.listar(busqueda.getValue()));
    }

    private void notificar(String msg, boolean error) {
        Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(error ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
    }
}
