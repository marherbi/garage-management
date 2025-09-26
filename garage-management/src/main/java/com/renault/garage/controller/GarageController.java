package com.renault.garage.controller;

import com.renault.garage.dto.CreateGarageDto;
import com.renault.garage.dto.GarageDto;
import com.renault.garage.service.GarageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/garages")
@Tag(name = "Garages", description = "Operations sur les garages")
public class GarageController {

    private final GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un garage par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garage trouvé", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GarageDto.class))),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé", content = @Content)
    })
    public ResponseEntity<GarageDto> findById(@Parameter(description = "Identifiant du garage", required = true) @PathVariable long id) {
        GarageDto garageDto = garageService.getGarageById(id);
        if (garageDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(garageDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un garage par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Garage supprimé", content = @Content),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé", content = @Content)
    })
    public ResponseEntity<Void> deleteById(@Parameter(description = "Identifiant du garage", required = true) @PathVariable long id) {
        boolean deleted = garageService.deleteGarage(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau garage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Garage créé", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GarageDto.class)))
    })
    public ResponseEntity<GarageDto> createGarage(@RequestBody CreateGarageDto garageDto) {
        GarageDto created = garageService.createGarage(garageDto);
        if (created == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(URI.create("/api/v1/garages/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un garage (remplacement partiel ou complet des champs fournis)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garage mis à jour", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GarageDto.class))),
            @ApiResponse(responseCode = "404", description = "Garage non trouvé", content = @Content)
    })
    public ResponseEntity<GarageDto> updateGarage(@PathVariable long id, @RequestBody CreateGarageDto updateDto) {
        GarageDto updated = garageService.updateGarage(id, updateDto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<Page<GarageDto>> listGarages(@Parameter(description = "Index de page (0..n)") @RequestParam(value = "page", defaultValue = "0") int page,
                                                       @Parameter(description = "Taille de page") @RequestParam(value = "size", defaultValue = "10") int pageSize,
                                                       @Parameter(description = "Paramètres de tri: champ;[asc,desc]") @RequestParam(value = "sort", required = false) List<String> sort) {
        List<Order> sortOrders;
        if (sort != null) {
            sortOrders = sort.stream()
                    .filter(s -> s.split(";").length == 2)
                    .map(s -> s.split(";"))
                    .filter(array -> isValidSort(array[1], array[0]))
                    .map(array -> new Order(Direction.fromString(array[1]), array[0]))
                    .toList();
        } else {
            sortOrders = List.of(Order.by("id"));
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortOrders));
        Page<GarageDto> garages = garageService.getGarages(pageable);
        return ResponseEntity.ok(garages);
    }

    @GetMapping("byVehicleFuelType/{vehicleFuelType}")
    public ResponseEntity<Page<GarageDto>> listGaragesByVehicleFuelType(
            @Parameter(description = "Type de carburant du véhicule") @PathVariable String vehicleFuelType,
            @Parameter(description = "Index de page (0..n)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @Parameter(description = "Paramètres de tri: champ;[asc,desc]") @RequestParam(value = "sort", required = false) List<String> sort) {
        List<Order> sortOrders;
        if (sort != null) {
            sortOrders = sort.stream()
                    .filter(s -> s.split(";").length == 2)
                    .map(s -> s.split(";"))
                    .filter(array -> isValidSort(array[1], array[0]))
                    .map(array -> new Order(Direction.fromString(array[1]), array[0]))
                    .toList();
        } else {
            sortOrders = List.of(Order.by("id"));
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortOrders));
        Page<GarageDto> garages = garageService.getGaragesHavingVehiclesWithFuelType(vehicleFuelType, pageable);
        return ResponseEntity.ok(garages);
    }

    /**
     * Check if the sort direction and property are valid
     *
     * @param direction ASC or DESC
     * @param property  field to sort by
     * @return true if valid, false otherwise
     */
    private boolean isValidSort(String direction, String property) {
        Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "address", "telephone", "email", "id");
        return (StringUtils.equalsAnyIgnoreCase(direction, ASC.name(), DESC.name())
                && ALLOWED_SORT_FIELDS.contains(property));
    }
}
