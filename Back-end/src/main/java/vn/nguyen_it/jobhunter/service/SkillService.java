package vn.nguyen_it.jobhunter.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.nguyen_it.jobhunter.domain.Skill;
import vn.nguyen_it.jobhunter.domain.response.ResultPaginationDTO;
import vn.nguyen_it.jobhunter.repository.SkillRepository;

@Service
public class SkillService {
    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    // kiểm tra xem name skill có tồn tại trong database hay không
    public boolean isNameExist(String name) {
        return this.skillRepository.existsByName(name);
    }

    // lấy tất cả skill thông qua id
    public Skill fetchSkillById(long id) {
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        if (skillOptional.isPresent())
            return skillOptional.get();
        return null;
    }

    // tại mới một skill
    public Skill createSkill(Skill s) {
        return this.skillRepository.save(s);
    }

    // update một skill
    public Skill updateSkill(Skill s) {
        return this.skillRepository.save(s);
    }

    // xóa một skill
    public void deleteSkill(long id) {
        // delete job (inside job_skill table)
        Optional<Skill> skillOptional = this.skillRepository.findById(id);
        Skill currentSkill = skillOptional.get();
        currentSkill.getJobs().forEach(job -> job.getSkills().remove(currentSkill));
        // delete skill
        this.skillRepository.delete(currentSkill);
    }

    // phân trang skill
    public ResultPaginationDTO fetchAllSkills(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> pageUser = this.skillRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageUser.getContent());
        return rs;
    }
}