package com.backend.backend_boilerplate.domain.user;

import com.backend.backend_boilerplate.domain.user.dto.UserSearchCond;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.backend.backend_boilerplate.domain.user.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> search(UserSearchCond cond, Pageable pageable) {

        List<User> content = queryFactory
                .selectFrom(user)
                .join(user)
                .where(
                        nameContains(cond.getNameKeyword()),
                        emailContains(cond.getEmailKeyword())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        nameContains(cond.getNameKeyword()),
                        emailContains(cond.getEmailKeyword())
                ).fetchOne();

        long totalCount = (total == null) ? 0L : total;

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression nameContains(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : user.name.containsIgnoreCase(keyword);
    }

    private BooleanExpression emailContains(String keyword) {
        return (keyword == null || keyword.isBlank()) ? null : user.email.containsIgnoreCase(keyword);
    }
}
