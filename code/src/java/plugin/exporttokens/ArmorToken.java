package plugin.exporttokens;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.core.Constants;
import pcgen.core.Equipment;
import pcgen.core.PlayerCharacter;
import pcgen.io.ExportHandler;
import pcgen.io.exporttoken.Token;
import pcgen.util.BigDecimalHelper;
import pcgen.util.Delta;
import pcgen.util.Logging;

/**
 * Class deals with ARMOR Token
 */
public class ArmorToken extends Token
{

	/** Name of the Token */
	public static final String TOKENNAME = "ARMOR";

	/**
	 * Get the token name
	 * @return token name
	 */
	public String getTokenName()
	{
		return TOKENNAME;
	}

	/**
	 * @see pcgen.io.exporttoken.Token#getToken(java.lang.String, pcgen.core.PlayerCharacter, pcgen.io.ExportHandler)
	 */
	public String getToken(String tokenSource, PlayerCharacter pc, ExportHandler eh)
	{
		if (tokenSource.startsWith("ARMOR") && ((tokenSource.charAt(5) == '.') || Character.isDigit(tokenSource.charAt(5)))) {
			return getArmorToken(tokenSource, pc);
		}
		return "";
	}

	/**
	 * @param tokenSource
	 * @param pc
	 * @return token
	 *
	 */
	public static String getArmorToken(String tokenSource, PlayerCharacter pc)
	{
		return replaceTokenArmor(tokenSource, pc);
	}

	/**
	 * See the PCGen Docs on Token Syntax
	 *
	 * @param aString
	 * @param aPC
	 * @return int
	 */
	private static String replaceTokenArmor(String aString, PlayerCharacter aPC)
	{
		final StringTokenizer aTok = new StringTokenizer(aString, ".");
		final String[] tokens = new String[aTok.countTokens()];

		for (int i = 0; aTok.hasMoreTokens(); ++i)
		{
			tokens[i] = aTok.nextToken();
		}

		String property = "";

		// When removing old syntax, this if should be removed
		if (tokens.length > 0)
		{
			property = tokens[tokens.length - 1];
		}

		int equipped = 3;
		int index = 0;
		String type = "";
		String subtype = "";
		int merge = Constants.MERGE_ALL;

		for (int i = 0; i < tokens.length; ++i)
		{
			if ("ARMOR".equals(tokens[i]))
			{
				continue;
			}

			// When removing old syntax, delete this if
			else if (tokens[i].startsWith("ARMOR"))
			{
				try
				{
					index = Integer.parseInt(tokens[i].substring(5));
				}
				catch (NumberFormatException nfe)
				{
					index = 0;
				}

				Logging.errorPrint("Old syntax ARMORx will be replaced for ARMOR.x");
			}
			else if ("ALL".equals(tokens[i]))
			{
				equipped = 3;
			}

			// When removing old syntax, delete this if
			else if (tokens[i].startsWith("ALL"))
			{
				Logging.errorPrint("Old syntax ALLx will be replaced for ALL.x");

				index = Integer.parseInt(tokens[i].substring(3));
				equipped = 3;
			}
			else if ("EQUIPPED".equals(tokens[i]))
			{
				equipped = 1;
			}

			// When removing old syntax, delete this if
			else if (tokens[i].startsWith("EQUIPPED"))
			{
				Logging.errorPrint("Old syntax EQUIPPEDx will be replaced for EQUIPPED.x");

				index = Integer.parseInt(tokens[i].substring(8));
				equipped = 1;
			}
			else if ("NOT_EQUIPPED".equals(tokens[i]))
			{
				equipped = 2;
			}

			// When removing old syntax, delete this if
			else if (tokens[i].startsWith("NOT_EQUIPPED"))
			{
				Logging.errorPrint("Old syntax NOT_EQUIPPEDx will be replaced for NOT_EQUIPPED.x");

				index = Integer.parseInt(tokens[i].substring(12));
				equipped = 2;
			}
			else if (tokens[i].equals("MERGENONE"))
			{
				merge = Constants.MERGE_NONE;
			}
			else if (tokens[i].equals("MERGELOC"))
			{
				merge = Constants.MERGE_LOCATION;
			}
			else if (tokens[i].equals("MERGEALL"))
			{
				merge = Constants.MERGE_ALL;
			}
			else if (tokens[i].equals("ISTYPE"))
			{
				property = tokens[i] + "." + tokens[i + 1];

				break;
			}
			else if (tokens[i].equals("NAME"))
			{
				property = tokens[i];
				if (i < (tokens.length - 1))
				{
					property += "." + tokens[i + 1];
				}

				break;
			}
			else if (i < (tokens.length - 1))
			{
				try
				{
					index = Integer.parseInt(tokens[i]);
				}
				catch (NumberFormatException exc)
				{
					if ("".equals(type))
					{
						type = tokens[i];
					}
					else
					{
						subtype = tokens[i];
					}
				}
			}
			else
			{
				property = tokens[i];
			}
		}

		if ("".equals(type))
		{
			return _replaceTokenArmor(index, property, equipped, merge, aPC);
		}
		else if ("SUIT".equals(type))
		{
			return _replaceTokenArmorSuit(index, subtype, property, equipped, merge, aPC);
		}
		else if ("SHIRT".equals(type))
		{
			return _replaceTokenArmorShirt(index, subtype, property, equipped, merge, aPC);
		}
		else if ("SHIELD".equals(type))
		{
			return _replaceTokenArmorShield(index, subtype, property, equipped, merge, aPC);
		}
		else if ("ITEM".equals(type) || "ACITEM".equals(type))
		{
			return _replaceTokenArmorItem(index, subtype, property, equipped, merge, aPC);
		}
		else
		{
			return _replaceTokenArmorVarious(index, type, subtype, property, equipped, merge, aPC);
		}
	}

	/**
	 * select suits - shields
	 * @param armor
	 * @param property
	 * @param equipped
	 * @param merge
	 * @param aPC
	 * @return int
	 */
	private static String _replaceTokenArmor(int armor, String property, int equipped, int merge, PlayerCharacter aPC)
	{
		final List<Equipment> aArrayList = aPC.getEquipmentOfTypeInOutputOrder("Armor", equipped, merge);
		final List<Equipment> bArrayList = aPC.getEquipmentOfTypeInOutputOrder("Shield", equipped, merge);

		for ( Equipment eq : bArrayList )
		{
			aArrayList.remove(eq);
		}

		if (armor < aArrayList.size())
		{
			final Equipment eq = aArrayList.get(armor);
			return _writeArmorProperty(eq, property, aPC);
		}

		return "";
	}

	/**
	 * select items, which improve AC but are not type ARMOR
	 * @param item
	 * @param subtype
	 * @param property
	 * @param equipped
	 * @param merge
	 * @param aPC
	 * @return int
	 */
	private static String _replaceTokenArmorItem(int item, String subtype, String property, int equipped, int merge, PlayerCharacter aPC)
	{
		// select all pieces of equipment of status==equipped
		// filter all AC relevant stuff
		final List<Equipment> aArrayList = new ArrayList<Equipment>();

		for ( Equipment eq : aPC.getEquipmentListInOutputOrder(merge) )
		{
			if (("".equals(subtype) || eq.isType(subtype))
				&& ((equipped == 3) || ((equipped == 2) && !eq.isEquipped()) || ((equipped == 1) && eq.isEquipped())))
			{
				if (eq.getBonusListString("AC") && !eq.isArmor() && !eq.isShield())
				{
					aArrayList.add(eq);
				}
			}
		}

		if (item < aArrayList.size())
		{
			final Equipment eq = aArrayList.get(item);
			return _writeArmorProperty(eq, property, aPC);
		}

		return "";
	}

	/**
	 * select shields
	 * @param shield
	 * @param subtype
	 * @param property
	 * @param equipped
	 * @param merge
	 * @param aPC
	 * @return int
	 */
	private static String _replaceTokenArmorShield(int shield, String subtype, String property, int equipped, int merge, PlayerCharacter aPC)
	{
		final List<Equipment> aArrayList = aPC.getEquipmentOfTypeInOutputOrder("Shield", subtype, equipped, merge);

		if (shield < aArrayList.size())
		{
			final Equipment eq = aArrayList.get(shield);
			return _writeArmorProperty(eq, property, aPC);
		}

		return "";
	}

	/**
	 * select shirts
	 * @param shirt
	 * @param subtype
	 * @param property
	 * @param equipped
	 * @param merge
	 * @param aPC
	 * @return int
	 */
	private static String _replaceTokenArmorShirt(int shirt, String subtype, String property, int equipped, int merge, PlayerCharacter aPC)
	{
		final List<Equipment> aArrayList = aPC.getEquipmentOfTypeInOutputOrder("Shirt", subtype, equipped, merge);

		if (shirt < aArrayList.size())
		{
			final Equipment eq = aArrayList.get(shirt);
			return _writeArmorProperty(eq, property, aPC);
		}

		return "";
	}

	/**
	 * select suits
	 * @param suit
	 * @param subtype
	 * @param property
	 * @param equipped
	 * @param merge
	 * @param aPC
	 * @return int
	 */
	private static String _replaceTokenArmorSuit(int suit, String subtype, String property, int equipped, int merge, PlayerCharacter aPC)
	{
		final List<Equipment> aArrayList = aPC.getEquipmentOfTypeInOutputOrder("Suit", subtype, equipped, merge);

		//
		// Temporary hack until someone gets around to fixing it properly
		//
		//aArrayList.addAll(aPC.getEquipmentOfTypeInOutputOrder("Shirt", subtype, equipped));

		if (suit < aArrayList.size())
		{
			final Equipment eq = aArrayList.get(suit);
			return _writeArmorProperty(eq, property, aPC);
		}

		return "";
	}

	/**
	 * select various stuff, that improves AC
	 * @param index
	 * @param type
	 * @param subtype
	 * @param property
	 * @param equipped
	 * @param merge
	 * @param aPC
	 * @return int
	 */
	private static String _replaceTokenArmorVarious(int index, String type, String subtype, String property, int equipped,
										  int merge, PlayerCharacter aPC)
	{
		final List<Equipment> aArrayList = new ArrayList<Equipment>();

		for (Equipment eq : aPC.getEquipmentOfTypeInOutputOrder(type, subtype, equipped, merge) )
		{
			if (eq.getACMod(aPC).intValue() > 0)
			{
				aArrayList.add(eq);
			}
			else if (eq.getBonusListString("AC"))
			{
				aArrayList.add(eq);
			}
		}

		if (index < aArrayList.size())
		{
			final Equipment eq = aArrayList.get(index);
			return _writeArmorProperty(eq, property, aPC);
		}

		return "";
	}

	private static String _writeArmorProperty(Equipment eq, String property, PlayerCharacter aPC)
	{
		StringBuffer ret = new StringBuffer();

		if (property.startsWith("NAME"))
		{
			if (eq.isEquipped() && !property.equals("NAMENOSTAR"))
			{
				ret.append("*");
			}

			ret.append(eq.parseOutputName(eq.getOutputName(), aPC));
			ret.append(eq.getAppliedName());
		}
		else if (property.startsWith("OUTPUTNAME"))
		{
			// TODO this appears to be the same as above.  Should be refactored
			if (eq.isEquipped())
			{
				ret.append("*");
			}

			ret.append(eq.parseOutputName(eq.getOutputName(), aPC));
			ret.append(eq.getAppliedName());
		}
		else if (property.startsWith("TOTALAC"))
		{
			// adjustments for new equipment modifier
			// EQMARMOR|AC|x|TYPE=ENHANCEMENT changed to COMBAT|AC|x|TYPE=Armor.ENHANCEMENT
			//FileAccess.write(output, Delta.toString(eq.getACMod()));
			ret.append(Delta.toString((int) eq.bonusTo(aPC, "COMBAT", "AC", true)));
		}
		else if (property.startsWith("BASEAC"))
		{
			// adjustments for new equipment modifier
			// EQMARMOR|AC|x|TYPE=ENHANCEMENT changed to COMBAT|AC|x|TYPE=Armor.ENHANCEMENT
			//FileAccess.write(output, Delta.toString(eq.getACMod()));
			ret.append(Delta.toString((int) eq.bonusTo("COMBAT", "AC", aPC, aPC)));
		}
		else if (property.startsWith("ACBONUS"))
		{
			ret.append(Delta.toString((int) eq.bonusTo(aPC, "COMBAT", "AC", true)));
		}
		else if (property.startsWith("MAXDEX"))
		{
			final int iMax = eq.getMaxDex(aPC).intValue();

			if (iMax != Constants.MAX_MAXDEX)
			{
				ret.append(Delta.toString(iMax));
			}
		}
		else if (property.startsWith("ACCHECK"))
		{
			ret.append(Delta.toString(eq.acCheck(aPC)));
		}
		else if (property.startsWith("EDR"))
		{
			ret.append(Delta.toString(eq.eDR(aPC)));
		}
		else if (property.startsWith("ISTYPE"))
		{
			if (eq.isType(property.substring(property.indexOf(".") + 1))) {
				ret.append("TRUE");
			} else {
				ret.append("FALSE");
			}
		}
		else if (property.startsWith("SPELLFAIL"))
		{
			ret.append(eq.spellFailure(aPC).toString());
		}
		else if (property.startsWith("MOVE"))
		{
			final StringTokenizer aTok = new StringTokenizer(eq.moveString(), ",", false);
			String tempString = "";

			if (("M".equals(aPC.getSize()) || "S".equals(aPC.getSize())) && (aTok.countTokens() > 0))
			{
				tempString = aTok.nextToken();

				if ("S".equals(aPC.getSize()) && (aTok.countTokens() > 1))
				{
					tempString = aTok.nextToken();
				}
			}

			ret.append(tempString);
		}
		else if (property.startsWith("SPROP"))
		{
			ret.append(eq.getSpecialProperties(aPC));
		}
		else if (property.startsWith("TYPE"))
		{
			String typeString = "";

			if (eq.isLight())
			{
				typeString = "Light";
			}
			else if (eq.isMedium())
			{
				typeString = "Medium";
			}
			else if (eq.isHeavy())
			{
				typeString = "Heavy";
			}
			else if (eq.isShield())
			{
				typeString = "Shield";
			}
			else if (eq.isExtra())
			{
				typeString = "Extra";
			}

			ret.append(typeString);
		}
		else if (property.startsWith("WT"))
		{
			ret.append(BigDecimalHelper.trimZeros(eq.getWeight(aPC).toString()));
		}
		return ret.toString();
	}

}
