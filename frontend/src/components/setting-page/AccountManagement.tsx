import {
  SettingSection,
  SettingTitle,
} from '@/components/setting-page/SettingLayout';

interface AccountManagementProps {
  openDeleteAccount: () => void;
}

const AccountManagement = ({ openDeleteAccount }: AccountManagementProps) => {
  return (
    <SettingSection>
      <SettingTitle className="text-label-assistive">
        <button
          onClick={openDeleteAccount}
          className="body1-normal-bold text-label-assistive text-left"
        >
          계정 삭제
        </button>
      </SettingTitle>
    </SettingSection>
  );
};

export { AccountManagement };
