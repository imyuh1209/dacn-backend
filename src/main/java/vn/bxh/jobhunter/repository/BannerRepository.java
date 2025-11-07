package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.Banner;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long>, JpaSpecificationExecutor<Banner> {
    List<Banner> findAllByActiveTrueOrderByPositionAsc();
}