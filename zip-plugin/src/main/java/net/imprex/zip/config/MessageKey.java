package net.imprex.zip.config;

public enum MessageKey {

	Prefix("prefix", "§8[§eZIP§8] §7"),
	NotAConsoleCommand("notAConsoleCommand", "This command can not be executed from the console"),
	YouDontHaveTheFollowingPermission("youDontHaveTheFollowingPermission", "You Don't have the following permission §8\"§e{0}§8\""),
	ThisBackpackNoLongerExist("ThisBackpackNoLongerExist", "This backpack no longer exist"),
	ClickHereToSeeTheLatestRelease("clickHereToSeeTheLatestRelease", "Click here to see the latest release"),
	ANewReleaseIsAvailable("ANewReleaseIsAvailable", "A new version is available"),
	ClickHere("clickHere", "§f§l[CLICK HERE]"),
	CommandHelpStart("commandHelpStart", "§8[]§7========== §eZeroInventoryProblems §7==========§8[]"),
	CommandHelpPickup("commandHelpPickup", "§8/§7zip §epickup §8| §7Pickup inaccessible items§8."),
	CommandHelpLink("commandHelpLink", "§8/§7zip §elink §7<§ecancel§7> §8| §7Link multiple backpacks or cancel the request§8."),
	CommandHelpGive("commandHelpGive", "§8/§7zip §egive §7[§etype§7] §7<§eplayer§7> §8| §7Give yourself a backpack§8."),
	CommandHelpType("commandHelpType", "§8/§7zip §etype §8| §7Get a list of all backpacks§8."),
	CommandHelpEnd("commandHelpEnd", "§8[]§7========== §eZeroInventoryProblems §7==========§8[]"),
	CommandTypeStart("commandTypeStart", "§8[]§7========== §eZeroInventoryProblems Types §7==========§8[]"),
	CommandTypeContent("commandTypeContent", "  §8-§e{0}"),
	CommandTypeButtonGive("commandTypeButtonGive", "§7[§eGive§7]"),
	CommandTypeButtonGiveHover("commandTypeButtonGiveHover", "§eClick here to give yourself a §8\"§e{0}§8\" backpack"),
	CommandTypeEnd("commandTypeEnd", "§8[]§7========== §eZeroInventoryProblems Types §7==========§8[]"),
	NoOnlinePlayerWasFound("noOnlinePlayerWasFound", "No online player with the name §8\"§e{0}§8} §7was found"),
	PleaseEnterABackpackType("pleaseEnterABackpackType", "Please enter a backpack type §8(small/medium/big)"),
	BackpackTypeWasNotFound("backpackTypeWasNotFound", "Backpack type §8\"§e{0}§8\" §7was not found"),
	YouHaveGivenYourselfABackpack("youHaveGivenYourselfABackpack", "You received a §8\"§e{0}§8\" §7backpack"),
	YouHaveGivenTargetPlayerABackpack("youHaveGivenTargetPlayerABackpack", "You given a §8\"§e{0}§8\" §7backpack to §8\"§e{1}§8\""),
	YouNeedToHoldABackpackInYourHand("youNeedToHoldABackpackInYourHand", "You need to hold a backpack in your hand"),
	YourBackpackHasNoUnusableItems("yourBackpackHasNoUnusableItems", "Your backpack has no inaccessible items"),
	YouNeedMoreSpaceInYourInventory("youNeedMoreSpaceInYourInventory", "You need more space in your inventory"),
	TargetPlayerNeedMoreSpaceInYourInventory("targetPlayerNeedMoreSpaceInYourInventory", "§8\"§e{0}§8\" §7needs more space in his/her inventory"),
	YouReceivedAllUnusableItems("youReceivedAllUnusableItems", "You\'ve received all inaccessible items"),
	YouHaveUnusableItemsUsePickup("youHaveUnusableItemsUsePickup", "Your backpack contains inaccessible items§8! §7Use §e/zip pickup"),
	YourBackpackIsNotEmpty("yourBackpackIsNotEmpty", "Your Backpack needs to be empty"),
	YouNeedToHoldBothBackpacksInYouInventory("youNeedToHoldBothBackpacksInYourInventory", "You need to hold both backpacks in your inventory"),
	YouCanNowHoldTheBackpackWhichShouldBeLinked("youCanNowHoldTheBackpackWhichShouldBeLinked", "You can now hold the second backpack that should be linked"),
	ThisShouldNotHappenedPleaseTryToLinkAgain("thisShouldNotHappenedPleaseTryToLinkAgain", "An error occurred, please try to link again"),
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