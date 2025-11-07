package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Banner;
import vn.bxh.jobhunter.domain.response.ResBannerDTO;
import vn.bxh.jobhunter.repository.BannerRepository;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;

    public Banner create(Banner banner) {
        // basic validation per spec: title required, image required on create
        if (banner.getTitle() == null || banner.getTitle().trim().isEmpty()) {
            throw new IdInvalidException("Title is required");
        }
        if (banner.getImage() == null || banner.getImage().trim().isEmpty()) {
            throw new IdInvalidException("Image is required");
        }
        return bannerRepository.save(banner);
    }

    public Banner update(Banner banner) {
        if (banner.getId() == null) {
            throw new IdInvalidException("Id is not valid!");
        }
        Optional<Banner> existing = bannerRepository.findById(banner.getId());
        if (existing.isEmpty()) {
            throw new IdInvalidException("Id is not valid!");
        }
        Banner b = existing.get();
        if (banner.getTitle() == null || banner.getTitle().trim().isEmpty()) {
            throw new IdInvalidException("Title is required");
        }
        b.setTitle(banner.getTitle());
        b.setImage(banner.getImage());
        b.setLink(banner.getLink());
        b.setActive(banner.isActive());
        b.setStartDate(banner.getStartDate());
        b.setEndDate(banner.getEndDate());
        b.setPosition(banner.getPosition());
        return bannerRepository.save(b);
    }

    public void delete(Long id) {
        bannerRepository.deleteById(id);
    }

    public Banner get(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Id is not valid!"));
    }

    public List<ResBannerDTO> getHomeBanners() {
        Instant now = Instant.now();
        return bannerRepository.findAllByActiveTrueOrderByPositionAsc()
                .stream()
                .filter(b -> {
                    boolean afterStart = (b.getStartDate() == null) || !now.isBefore(b.getStartDate());
                    boolean beforeEnd = (b.getEndDate() == null) || !now.isAfter(b.getEndDate());
                    return afterStart && beforeEnd;
                })
                .map(this::toResDTO)
                .collect(Collectors.toList());
    }

    public ResultPaginationDTO getAll(Specification<Banner> spec, Pageable pageable) {
        Page<Banner> page = bannerRepository.findAll(spec, pageable);
        List<ResBannerDTO> rows = page.getContent().stream()
                .map(this::toResDTO)
                .collect(Collectors.toList());
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
        result.setMeta(meta);
        result.setResult(rows);
        return result;
    }

    public ResBannerDTO toResDTO(Banner banner) {
        ResBannerDTO dto = new ResBannerDTO();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImage(banner.getImage());
        dto.setLink(banner.getLink());
        dto.setActive(banner.isActive());
        dto.setStartDate(banner.getStartDate());
        dto.setEndDate(banner.getEndDate());
        dto.setOrder(banner.getPosition());
        dto.setPosition(banner.getPlacement() != null ? banner.getPlacement().name() : "HOME");
        dto.setCreatedAt(banner.getCreatedAt());
        dto.setUpdatedAt(banner.getUpdatedAt());
        dto.setCreatedBy(banner.getCreatedBy());
        dto.setUpdatedBy(banner.getUpdatedBy());
        return dto;
    }
}