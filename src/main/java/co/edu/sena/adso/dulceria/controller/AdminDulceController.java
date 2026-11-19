package co.edu.sena.adso.dulceria.controller;
 
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
import co.edu.sena.adso.dulceria.model.Categoria;
import co.edu.sena.adso.dulceria.model.Dulce;
import co.edu.sena.adso.dulceria.service.DulceService;
import jakarta.validation.Valid;
 
@Controller
@RequestMapping("/admin/dulces")
public class AdminDulceController {
 
    private final DulceService dulces;
 
    public AdminDulceController(DulceService dulces) {
        this.dulces = dulces;
    }
 
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("dulces", dulces.listar());
        return "admin/dulces";
    }
 
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("dulce", new Dulce());
        model.addAttribute("categorias", Categoria.values());
        return "admin/dulce-form";
    }
 
    // NUEVO · GET /admin/dulces/3/editar -> el MISMO formulario, lleno con los datos del dulce 3
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, Model model) {
        model.addAttribute("dulce", dulces.buscar(id));
        model.addAttribute("categorias", Categoria.values());
        return "admin/dulce-form";
    }
 
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("dulce") Dulce dulce,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            model.addAttribute("categorias", Categoria.values());
            return "admin/dulce-form";
        }
        boolean esNuevo = dulce.getId() == null;
        Dulce guardado = dulces.guardar(dulce);
        redireccion.addFlashAttribute("mensaje",
                (esNuevo ? "Se registró " : "Se actualizó ") + "«" + guardado.getNombre() + "».");
        return "redirect:/admin/dulces";
    }
 
    // NUEVO · POST /admin/dulces/3/eliminar
    // Se elimina con POST (nunca con un enlace GET) para que nadie borre datos por accidente.
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redireccion) {
        try {
            dulces.eliminar(id);
            redireccion.addFlashAttribute("mensaje", "El dulce fue eliminado.");
        } catch (DataIntegrityViolationException e) {
            // PostgreSQL no deja borrar un dulce que tiene encargos (llave foránea, etapa 12)
            redireccion.addFlashAttribute("error", "No se puede eliminar: el dulce tiene encargos. Márquelo como agotado.");
        }
        return "redirect:/admin/dulces";
    }
}