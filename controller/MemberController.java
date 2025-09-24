package com.example.familyfinance.controller;

import com.example.familyfinance.entity.Member;
import com.example.familyfinance.entity.User;
import com.example.familyfinance.repository.MemberRepository;
import com.example.familyfinance.repository.UserRepository;
import com.example.familyfinance.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncomeService incomeService;

    // 获取某个家庭的所有成员
    @GetMapping("/user/{userId}")
    public List<Member> getMembersByUser(@PathVariable Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return memberRepository.findByUser(user);
    }

    // 添加家庭成员
    @PostMapping
    public Member addMember(@RequestBody Member member) {
        return memberRepository.save(member);
    }

    // 修改家庭成员
    @PutMapping("/{id}")
    public Member updateMember(@PathVariable Integer id, @RequestBody Member updatedMember) {
        Member member = memberRepository.findById(id).orElseThrow();
        member.setName(updatedMember.getName());
        member.setRole(updatedMember.getRole());
        member.setGender(updatedMember.getGender());
        member.setBirthDate(updatedMember.getBirthDate());
        member.setPhone(updatedMember.getPhone());
        member.setRemark(updatedMember.getRemark());
        return memberRepository.save(member);
    }

    // 删除家庭成员
    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Integer id) {
        memberRepository.deleteById(id);
    }

    /**
     * 计算家庭人均收入
     * mode 参数可选：
     *   lastYear   -> 按去年
     *   thisYear   -> 按今年
     *   last12     -> 按近12个月
     */
    @GetMapping("/user/{userId}/perCapitaIncome")
    public Double getPerCapitaIncome(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "thisYear") String mode) {

        switch (mode) {
            case "lastYear":
                return incomeService.calculatePerCapitaIncomeLastYear(userId);
            case "last12":
                return incomeService.calculatePerCapitaIncomeLast12Months(userId);
            case "thisYear":
            default:
                return incomeService.calculatePerCapitaIncomeThisYear(userId);
        }
    }
}
