package com.lime.mukbbomap.controller;


 import com.lime.mukbbomap.index.RestaurantQuadTreeIndex;
 import org.springframework.http.MediaType;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RestController;

 @RestController
 public class TreeDebugController {
     private final RestaurantQuadTreeIndex index;
     public TreeDebugController(RestaurantQuadTreeIndex index) { this.index = index; }

     @GetMapping(value = "/api/debug/tree", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
     public String tree() {
         return index.dumpTree();
     }
 }