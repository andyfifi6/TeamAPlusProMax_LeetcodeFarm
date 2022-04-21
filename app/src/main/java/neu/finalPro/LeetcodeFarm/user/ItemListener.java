package neu.finalPro.LeetcodeFarm.user;

import neu.finalPro.LeetcodeFarm.models.ChatMessage;
import neu.finalPro.LeetcodeFarm.models.User;

public interface ItemListener {
    void onUserClicked(User user);
    void onChatClicked(ChatMessage chatMessage);
}
