package com.study.tddmembership.membership.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author 공통플랫폼팀 오경환
 * @version 1.0 <pre> 2022.04.05 : 최초 작성 </pre>
 * @since 2022.04.05
 */
@Getter
@RequiredArgsConstructor
public class MembershipException extends RuntimeException{

	private final MembershipErrorResult errorResult;

}