package com.renault.garage.controller;

import com.renault.garage.dto.AccessoryDto;
import com.renault.garage.dto.CreateAccessoryDto;
import com.renault.garage.service.AccessoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Accessoires", description = "Opérations sur les accessoires des véhicules")
public class AccessoryController {

    private final AccessoryService accessoryService;

    public AccessoryController(AccessoryService accessoryService) {
        this.accessoryService = accessoryService;
    }

    @Operation(summary = "Créer un accessoire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Accessoire créé",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    @PostMapping(value = "/vehicles/{vehicleId}/accessories", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessoryDto> createAccessory(@PathVariable long vehicleId, @RequestBody CreateAccessoryDto accessoryDto) {
        AccessoryDto created = accessoryService.createAccessory(vehicleId, accessoryDto);
        return ResponseEntity.created(URI.create("/api/v1/accessories/" + created.id())).body(created);
    }

    @Operation(summary = "Récupérer tous les accessoires d'un véhicule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des accessoires",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessoryDto.class)))
    })
    @GetMapping(value = "/vehicles/{vehicleId}/accessories", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccessoryDto>> getAllAccessoriesByVehicleId(@PathVariable long vehicleId) {
        return ResponseEntity.ok(accessoryService.getAllByVehicleId(vehicleId));
    }

    @Operation(summary = "Récupérer un accessoire par son id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessoire trouvé",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Accessoire non trouvé", content = @Content)
    })
    @GetMapping(value = "/accessories/{accessoryId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessoryDto> getAccessoryById(@PathVariable Long accessoryId) {
        AccessoryDto accessoryById = accessoryService.getAccessoryById(accessoryId);
        if (accessoryById == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(accessoryById);
    }

    @Operation(summary = "Mettre à jour un accessoire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessoire mis à jour",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Accessoire non trouvé", content = @Content)
    })
    @PutMapping(value = "/accessories/{accessoryId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessoryDto> updateAccessory(@PathVariable Long accessoryId, @RequestBody CreateAccessoryDto accessoryDto) {
        AccessoryDto updatedAccessory = accessoryService.updateAccessory(accessoryId, accessoryDto);
        if (updatedAccessory == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updatedAccessory);
    }

    @Operation(summary = "Supprimer un accessoire")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Accessoire supprimé"),
            @ApiResponse(responseCode = "404", description = "Accessoire non trouvé", content = @Content)
    })
    @DeleteMapping(value = "/accessories/{accessoryId}")
    public ResponseEntity<Void> deleteAccessory(@PathVariable Long accessoryId) {
        if (accessoryService.deleteAccessory(accessoryId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
