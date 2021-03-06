package com.study.tddmembership.membership.service;

import com.study.tddmembership.enums.MembershipType;
import com.study.tddmembership.membership.entity.Membership;
import com.study.tddmembership.membership.exception.MembershipErrorResult;
import com.study.tddmembership.membership.exception.MembershipException;
import com.study.tddmembership.membership.repository.MembershipRepository;
import com.study.tddmembership.membership.dto.MembershipDetailResponse;
import com.study.tddmembership.membership.dto.MembershipResponse;
import com.study.tddmembership.point.service.RatePointService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

  private final String userId = "userId";
  private final Long membershipId = -1L;
  private final MembershipType membershipType = MembershipType.NAVER;
  private final Integer point = 10000;

  @InjectMocks private MembershipService target;

  @Mock private MembershipRepository membershipRepository;

  @Mock private RatePointService ratePointService;

  @Test
  @DisplayName("멤버쉽등록성공")
  void membershipAddSuccess() {
    // given
    doReturn(null).when(membershipRepository).findByUserIdAndMembershipType(userId, membershipType);
    doReturn(membership()).when(membershipRepository).save(any(Membership.class));

    // when

    final MembershipResponse result = target.addMembership(userId, membershipType, point);

    // then
    assertThat(result.getId()).isNotNull();
    assertThat(result.getMembershipType()).isEqualTo(MembershipType.NAVER);

    // verify
    verify(membershipRepository, times(1)).findByUserIdAndMembershipType(userId, membershipType);
    verify(membershipRepository, times(1)).save(any(Membership.class));
  }

  private Membership membership() {
    return Membership.builder()
        .id(-1L)
        .userId(userId)
        .point(point)
        .membershipType(MembershipType.NAVER)
        .build();
  }

  @Test
  @DisplayName("멤버십 목록조회 성공")
  void membershipListSearchSuccess() {
    // given
    doReturn(
            Arrays.asList(
                Membership.builder().build(),
                Membership.builder().build(),
                Membership.builder().build()))
        .when(membershipRepository)
        .findAllByUserId(userId);

    // when
    final List<MembershipDetailResponse> result = target.getMembershipList(userId);

    // then
    assertThat(result.size()).isEqualTo(3);
  }

  @Test
  @DisplayName("멤버십 상세조회 성공")
  void membershipSearchDetailSuccess(){
    // given
    doReturn(Optional.of(membership())).when(membershipRepository).findById(membershipId);

    // when
    final  MembershipDetailResponse result = target.getMembership(membershipId,userId);

    // then
    assertThat(result.getMembershipType()).isEqualTo(MembershipType.NAVER);
    assertThat(result.getPoint()).isEqualTo(point);

  }

  @Test
  @DisplayName("맴버쉽등록실패 이미존재함")
  void memberAddFailDuplication() {
    // given
    doReturn(Membership.builder().build())
        .when(membershipRepository)
        .findByUserIdAndMembershipType(userId, membershipType);

    final MembershipException result =
        assertThrows(
            MembershipException.class, () -> target.addMembership(userId, membershipType, point));

    assertThat(result.getErrorResult())
        .isEqualTo(MembershipErrorResult.DUPLICATED_MEMBERSHIP_REGISTER);
  }

  @Test
  @DisplayName("멤버십상세조회실패 존재하지않음")
  void membershipSearchFailByEmpty() {
    // given
    doReturn(Optional.empty()).when(membershipRepository).findById(membershipId);

    // when
    final MembershipException result =
        assertThrows(MembershipException.class, () -> target.getMembership(membershipId, userId));

    // then
    assertThat(result.getErrorResult()).isEqualTo(MembershipErrorResult.MEMBERSHIP_NOT_FOUND);
  }

  @Test
  @DisplayName("멤버쉽상세조회실패 본인이 아님")
  void membershipSearchFailByWrongUserId() {
    // given
    doReturn(Optional.empty()).when(membershipRepository).findById(membershipId);

    // when
    final MembershipException result =
        assertThrows(
            MembershipException.class, () -> target.getMembership(membershipId, "notowner"));

    // then
    assertThat(result.getErrorResult()).isEqualTo(MembershipErrorResult.MEMBERSHIP_NOT_FOUND);
  }

  @Test
  @DisplayName("멤버십삭제실패 존재하지않음")
  void membershipDeleteFailByEmpty() {
    // given
    doReturn(Optional.empty()).when(membershipRepository).findById(membershipId);

    // when
    final MembershipException result = assertThrows(MembershipException.class,
        () -> target.removeMembership(membershipId, userId));

    // then
    assertThat(result.getErrorResult()).isEqualTo(MembershipErrorResult.MEMBERSHIP_NOT_FOUND);
  }

  @Test
  @DisplayName("멤버십삭제실패 본인이아님")
  void membershipDeleteFailByNotOwner() {
    // given
    final Membership membership = membership();
    doReturn(Optional.of(membership)).when(membershipRepository).findById(membershipId);

    // when
    final MembershipException result = assertThrows(MembershipException.class,
        () -> target.removeMembership(membershipId, "notowner"));

    // then
    assertThat(result.getErrorResult()).isEqualTo(MembershipErrorResult.NOT_MEMBERSHIP_OWNER);
  }

  @Test
  @DisplayName("멤버십삭제성공")
  void membershipDeleteSuccess() {
    // given
    final Membership membership = membership();
    doReturn(Optional.of(membership)).when(membershipRepository).findById(membershipId);

    // when
    target.removeMembership(membershipId, userId);

    // then
  }

  @Test
  @DisplayName("멤버십적립성공")
  public void membershipCollectSuccess() {
    // given
    final Membership membership = membership();
    doReturn(Optional.of(membership)).when(membershipRepository).findById(membershipId);

    // when
    target.accumulateMembershipPoint(membershipId, userId, 10000);

    // then
  }

  @Test
  @DisplayName("멤버십적립실패 존재하지않음")
  void membershipCollectFailByEmpty(){
    // given
    doReturn(Optional.empty()).when(membershipRepository).findById(membershipId);

    // when
    final MembershipException result = assertThrows(
        MembershipException.class, () -> target.accumulateMembershipPoint(membershipId,userId,10000));

    // then
    assertThat(result.getErrorResult()).isEqualTo(MembershipErrorResult.MEMBERSHIP_NOT_FOUND);

  }
}
