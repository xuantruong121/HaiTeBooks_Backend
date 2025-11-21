package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ✅ Tối ưu: Eager fetch category khi tìm theo barcode
    @EntityGraph(attributePaths = {"category"})
    Optional<Book> findByBarcode(String barcode);

    // ✅ Tìm kiếm theo tiêu đề (có hỗ trợ phân trang)
    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    // ✅ Tối ưu: Eager fetch category khi lấy tất cả sách
    @EntityGraph(attributePaths = {"category"})
    @Override
    List<Book> findAll();
    
    // ✅ Tối ưu: Eager fetch category khi lấy sách có phân trang
    @EntityGraph(attributePaths = {"category"})
    @Override
    Page<Book> findAll(Pageable pageable);
    
    // ✅ Tối ưu: Eager fetch category khi tìm theo ID
    @EntityGraph(attributePaths = {"category"})
    @Override
    Optional<Book> findById(Long id);
}
