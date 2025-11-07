package vn.bxh.jobhunter.controller;

import lombok.AllArgsConstructor;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.Banner;
import vn.bxh.jobhunter.domain.request.ReqBannerUpsert;
import vn.bxh.jobhunter.domain.response.ResBannerDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.service.BannerService;
import vn.bxh.jobhunter.util.anotation.ApiMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class BannerController {
    private final BannerService bannerService;

    @PostMapping("/banners")
    @ApiMessage("Create a banner")
    public ResponseEntity<Banner> create(@RequestBody ReqBannerUpsert req) {
        Banner banner = new Banner();
        banner.setTitle(req.getTitle());
        banner.setImage(req.getImage());
        banner.setLink(req.getLink());
        banner.setActive(req.isActive());
        banner.setStartDate(req.getStartDate());
        banner.setEndDate(req.getEndDate());
        banner.setPosition(req.getOrder() != null ? req.getOrder() : 0);
        // placement
        banner.setPlacement(req.getPosition());
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.create(banner));
    }

    @PutMapping("/banners")
    @ApiMessage("Update a banner")
    public ResponseEntity<Banner> update(@RequestBody ReqBannerUpsert req) {
        Banner banner = new Banner();
        banner.setId(req.getId());
        banner.setTitle(req.getTitle());
        banner.setImage(req.getImage());
        banner.setLink(req.getLink());
        banner.setActive(req.isActive());
        banner.setStartDate(req.getStartDate());
        banner.setEndDate(req.getEndDate());
        banner.setPosition(req.getOrder() != null ? req.getOrder() : 0);
        banner.setPlacement(req.getPosition());
        return ResponseEntity.ok(bannerService.update(banner));
    }

    @DeleteMapping("/banners/{id}")
    @ApiMessage("Accepted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @GetMapping("/banners/{id}")
    @ApiMessage("Get a banner")
    public ResponseEntity<Banner> get(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.get(id));
    }

    @GetMapping("/banners/home")
    @ApiMessage("Get active home banners")
    public ResponseEntity<List<ResBannerDTO>> home() {
        return ResponseEntity.ok(bannerService.getHomeBanners());
    }

    @GetMapping("/banners")
    @ApiMessage("Get banners with pagination")
    public ResponseEntity<ResultPaginationDTO> list(@Filter Specification<Banner> spec, Pageable pageable) {
        return ResponseEntity.ok(bannerService.getAll(spec, pageable));
    }
}