package vn.nguyen_it.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.nguyen_it.jobhunter.domain.Permission;
import vn.nguyen_it.jobhunter.domain.Role;
import vn.nguyen_it.jobhunter.domain.response.ResultPaginationDTO;
import vn.nguyen_it.jobhunter.repository.PermissionRepository;
import vn.nguyen_it.jobhunter.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // kiểm tra xem tên role có tồn tại trong database hay không
    public boolean existByName(String name) {
        return this.roleRepository.existsByName(name);
    }

    // tạo ra role mới(check xem role có hợp lệ không, ví dụ tạo một role mà không
    // có quyền thì lỗi)
    // nếu có quyền thì lấy những id của các quyền đó rồi truy vấn xuống database
    // rồi gán lại cho role
    // tức là khi có role thì phải có quyền đi kèm
    public Role create(Role r) {
        // check permissions
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }
        return this.roleRepository.save(r);
    }

    // truy vấn xuống database để lấy được role thông qua id truyền vào
    public Role fetchById(long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (roleOptional.isPresent())
            return roleOptional.get();
        return null;
    }

    // muốn update role thì phải check role đó có thực sự tồn tại trong database hay
    // không nếu có thì check xem role đó có những quyền hạn gì rồi truy vấn xuống
    // database để xem những quyền đó có tồn tại không rồi mới update lại role và
    // save vào database
    public Role update(Role r) {
        Role roleDB = this.fetchById(r.getId());
        // check permissions
        if (r.getPermissions() != null) {
            List<Long> reqPermissions = r.getPermissions().stream().map(x -> x.getId()).collect(Collectors.toList());
            List<Permission> dbPermissions = this.permissionRepository.findByIdIn(reqPermissions);
            r.setPermissions(dbPermissions);
        }
        roleDB.setName(r.getName());
        roleDB.setDescription(r.getDescription());
        roleDB.setActive(r.isActive());
        roleDB.setPermissions(r.getPermissions());
        roleDB = this.roleRepository.save(roleDB);
        return roleDB;
    }

    public void delete(long id) {
        this.roleRepository.deleteById(id);
    }

    // phân trang role  
    public ResultPaginationDTO getRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pRole = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pRole.getTotalPages());
        mt.setTotal(pRole.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pRole.getContent());
        return rs;
    }
}