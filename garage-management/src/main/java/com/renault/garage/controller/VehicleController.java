package com.renault.garage.controller;

import com.renault.garage.dto.CreateVehicleDto;
import com.renault.garage.dto.VehicleDto;
import com.renault.garage.exception.GarageCapacityExceededException;
import com.renault.garage.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Véhicules", description = "Opérations sur les véhicules")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("garages/{garageId}/vehicles")
    @Operation(summary = "Créer un véhicule")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Véhicule créé", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateVehicleDto.class)))
            , @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
            , @ApiResponse(responseCode = "422", description = "Capacité du garage dépassée", content = @Content)})
    public ResponseEntity<?> create(@Parameter(description = "Identifiant du garage") @PathVariable Long garageId, @RequestBody CreateVehicleDto vehicleDto) {
        try {
            VehicleDto created = vehicleService.create(garageId, vehicleDto);
            if (created == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.created(URI.create("/api/v1/vehicles/" + created.id())).body(created);
        } catch (GarageCapacityExceededException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    @PatchMapping("garages/{garageId}/vehicles/linking")
    @Operation(summary = "Lier un véhicule existant à un garage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tous les Véhicules liés au garage", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDto.class))),
            @ApiResponse(responseCode = "404", description = "Garage ou véhicule non trouvé", content = @Content),
            @ApiResponse(responseCode = "422", description = "Capacité du garage dépassée", content = @Content)})
    public ResponseEntity<?> linkVehicleToGarage(@PathVariable Long garageId, @RequestParam Long vehicleId) {
        try {
            List<VehicleDto> garageVehicles = vehicleService.linkVehicleToGarage(garageId, vehicleId);
            if (CollectionUtils.isEmpty(garageVehicles)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(garageVehicles);
        } catch (GarageCapacityExceededException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());

        }

    }

    @GetMapping("vehicles/{id}")
    @Operation(summary = "Récupérer un véhicule par son id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trouvé", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDto.class))),
            @ApiResponse(responseCode = "404", description = "Non trouvé", content = @Content)
    })
    public ResponseEntity<VehicleDto> get(@Parameter(description = "Identifiant véhicule") @PathVariable Long id) {
        VehicleDto dto = vehicleService.get(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("vehicles")
    @Operation(summary = "Lister tous les véhicules")
    public ResponseEntity<List<VehicleDto>> listAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @PutMapping("vehicles/{id}")
    @Operation(summary = "Mettre à jour un véhicule (remplace les champs fournis)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mis à jour", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDto.class))),
            @ApiResponse(responseCode = "404", description = "Non trouvé", content = @Content)
    })
    public ResponseEntity<VehicleDto> update(@PathVariable Long id, @RequestBody CreateVehicleDto dto) {
        VehicleDto updated = vehicleService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("vehicles/{id}")
    @Operation(summary = "Supprimer un véhicule")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Non trouvé", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = vehicleService.delete(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("garage/{garageId}/vehicles")
    @Operation(summary = "Lister les véhicules d'un garage par son id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des véhicules", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDto.class))),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé", content = @Content)
    })
    public ResponseEntity<List<VehicleDto>> listByGarageId(@PathVariable Long garageId) {
        List<VehicleDto> vehicles = vehicleService.getByGarage(garageId);
        if (vehicles == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("vehicles/byBrand/{brand}")
    @Operation(summary = "Lister les véhicules d’un modèle donné dans plusieurs garages")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des véhicules", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDto.class))),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé", content = @Content)
    })
    public ResponseEntity<List<VehicleDto>> listByBrand(@PathVariable String brand) {
        List<VehicleDto> vehicles = vehicleService.getByBrand(brand);
        if (vehicles == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("vehicles/byAccessory/{accessory}")
    @Operation(summary = "Lister les véhicules qui ont un accessoire donné")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des véhicules", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDto.class))),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé", content = @Content)
    })
    public ResponseEntity<List<VehicleDto>> listByAccessory(@PathVariable String accessory) {
        List<VehicleDto> vehicles = vehicleService.getByAccessory(accessory);
        if (CollectionUtils.isEmpty(vehicles)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vehicles);
    }


}
