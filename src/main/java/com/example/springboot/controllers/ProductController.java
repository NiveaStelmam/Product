package com.example.springboot.controllers;


import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;

import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController (ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //@Operation(summary = "Cadastrar", description = "Método que cadastra um novo produto", tags ="Products") // swagger
    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel); // converte o dto em model(type)
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));

    }

    //@Operation(summary = "Listar Todos", description = "Método que lista todos os produtos", tags ="Products") // swagger
    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts(){
       List<ProductModel> productsList = productRepository.findAll();
       if(!productsList.isEmpty()){
           for(ProductModel product : productsList){ // Para cada produto da lista, obtem o id e cria um link para cada atributo da lista
               UUID id = product.getIdProduct();
               product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
           }
       }
       return ResponseEntity.status(HttpStatus.OK).body(productsList);
    }

    //@Operation(summary = "Listar produto por ID", description = "Método que lista um produto baseado em seu ID", tags ="Products") // swagger
    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct (@PathVariable (value="id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);
        if(productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(productO.get());
    }

    //@Operation(summary = "Atualizar", description = "Método para atualizar um produto", tags ="Products") // swagger
    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value="id") UUID id, @RequestBody @Valid ProductRecordDto productRecordDto){
        Optional<ProductModel> productO = productRepository.findById(id); // busca o produto no banco de dados
        if(productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        var productModel = productO.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    //@Operation(summary = "Delete", description = "Método para excluir um produto", tags ="Products") // swagger
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value="id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);
        if(productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productRepository.delete(productO.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }

}
