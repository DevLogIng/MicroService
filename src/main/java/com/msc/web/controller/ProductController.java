package com.msc.web.controller;

import com.msc.dao.ProductDao;
import com.msc.model.Product;
import com.msc.web.exceptions.ProduitGratuitException;
import com.msc.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


@Api( description="API pour les opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits
    @ApiOperation(value = "Récupère la liste des produits en stock!")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")

    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }

    //ajouter un produit
    @PostMapping(value = "/Produits")
    @ApiOperation(value = "Ajouter un nouveau produit !")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

		if(product.getPrix()==0)throw new ProduitGratuitException("Le prix du produit ne doit pas être 0 !");
		
    	Product productAdded =  productDao.save(product);

        if (productAdded == null) 
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Supprimer un produit du stock !")
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

       // productDao.delete(id);
    }

    @ApiOperation(value = "Modifier un produit!")
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }


    //Pour les tests
    @ApiOperation(value = "Récupérer produits à partir du prix !")
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }
    // Calcule de la marge de chaque produit 
	@ApiOperation(value = "Calcule la marge de chaque produit (différence entre prix d‘achat et prix de vente) !")
    @GetMapping(value = "/AdminProduits")
    public Map<Product,Integer> calculerMargeProduit () {
    	List <Product> listofProduct = productDao.findAll();
    	Map<Product,Integer> list=new HashMap<Product, Integer>();
    	for(Product p:listofProduct) {
    		list.put(p, (p.getPrix()-p.getPrixAchat()));
    	}
    	return list;
    }

 // Fonction qui retourne  la liste de tous les produits triés par nom croissant
    @ApiOperation(value = "Trier la liste des produits en ordre alphabétique croissant !")
    @GetMapping(value = "/TriProduits")
    public List<Product> trierProduitsParOrdreAlphabetique (){
    	return productDao.trierProduitsParOrdreAlphabetique();
    }

}
