package net.imprex.zip.config;

public enum MessageKey {

	Prefix("prefix", "§8[§eZeroInventoryProblems§8] §7"),
	NotAConsoleCommand("notAConsoleCommand", "This command can not be executed from the console"),
	YouDontHaveTheFollowingPermission("youDontHaveTheFollowingPermission", "You Don't have the folloing permission §8\"§e{0}§8\""),
	AErrorOccured("AErrorOccured", "A error occured"),
	ClickHereToSeeTheLatestRelease("clickHereToSeeTheLatestRelease", "Click here to see the latest release"),
	ANewReleaseIsAvailable("ANewReleaseIsAvailable", "A new release is available"),
	ClickHere("clickHere", "§f§l[CLICK HERE]"),
	CommandHelpStart("commandHelpStart", "§8[]§7========== §eZeroInventoryProblems §7==========§8[]"),
	CommandHelpLine1("commandHelpLine1", "§8/§7zip §epickup §8| §7Pickup unaccessable items§8."),
	CommandHelpLine2("commandHelpLine2", "§8/§7zip §elink §8| §7Link multiply backpacks§8."),
	CommandHelpLine3("commandHelpLine3", "§8/§7zip §ecancel §8| §7Cancel A backpack link request§8."),
	CommandHelpEnd("commandHelpEnd", "§8[]§7========== §eZeroInventoryProblems §7==========§8[]"),
	YouNeedToHoldABackpackInYourHand("youNeedToHoldABackpackInYourHand", "You need to hold a backpack in your hand"),
	YourBackpackHasNoUnuseableItems("yourBackpackHasNoUnuseableItems", "Your backpack has no unuseable items"),
	YouNeedMoreSpaceInYourInventory("youNeedMoreSpaceInYourInventory", "You need more space in your inventory"),
	YouRecivedAllUnuseableItems("youRecivedAllUnuseableItems", "You recived all unuseable items"),
	YouHaveUnuseableItemsUsePickup("youHaveUnuseableItemsUsePickup", "You have unuseable items§8! §7Use §e/zip pickup"),
	YourBackpackIsNotEmpty("yourBackpackIsNotEmpty", "Your Backpack is not empty"),
	YouNeedToHoldBothBackpacksInYouInventory("youNeedToHoldBothBackpacksInYourInventory", "You need to hold both backpacks in your inventory"),
	YouCanNowHoldTheBackpackWichShoudBeLinked("youCanNowHoldTheBackpackWichShoudBeLinked", "You can now hold your backpack wich shoud be linked"),
	ThisShoudNotHappendPleaseTryToLinkAgain("thisShoudNotHappendPleaseTryToLinkAgain", "This shoud not be happening, please try to link again"),
	YourBackpackIsNowLinked("yourBackpackIsNowLinked", "Your backpack is now linked"),
	YouNeedToLinkABackpackFirst("youNeedToLinkABackpackFirst", "You need to link a backpack at first"),
	YourBackpackLinkRequestWasCancelled("yourBackpackLinkRequestWasCancelled", "Your backpack link request was cancelled"),
	BothBackpacksNeedToBeTheSameType("bothBackpacksNeedToBeTheSameType", "Both Backpacks need to be the same type"),
	ThisBackpackIsAlreadyLinkedThoThat("thisBackpackIsAlreadyLinkedThoThat", "This backpack is already linked to that backpack");

	public static MessageKey findByKey(String key) {
		for (MessageKey messageKey : values()) {
			if (messageKey.key.equalsIgnoreCase(key)) {
				return messageKey;
			}
		}
		return null;
	}

	private final String key;
	private final String defaultMessage;

	private MessageKey(String key, String defaultMessage) {
		this.key = key;
		this.defaultMessage = defaultMessage;
	}

	public String getKey() {
		return this.key;
	}

	public String getDefaultMessage() {
		return this.defaultMessage;
	}
}