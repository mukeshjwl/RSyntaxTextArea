/*
 * 03/23/2015
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSyntaxTextArea.License.txt file for details.
 */
package org.fife.ui.rsyntaxtextarea.modes;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for the {@link HTMLTokenMaker} class.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class HTMLTokenMakerTest {


	@Test
	public void testHtml_comment() {

		String[] commentLiterals = {
			"<!-- Hello world -->",
		};

		for (String code : commentLiterals) {
			Segment segment = new Segment(code.toCharArray(), 0, code.length());
			HTMLTokenMaker tm = new HTMLTokenMaker();
			Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);
			Assert.assertEquals(TokenTypes.MARKUP_COMMENT, token.getType());
		}

	}


	@Test
	public void testHtml_comment_URL() {

		String code = "<!-- Hello world http://www.google.com -->";
		Segment segment = new Segment(code.toCharArray(), 0, code.length());
		HTMLTokenMaker tm = new HTMLTokenMaker();
		Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);

		Assert.assertFalse(token.isHyperlink());
		Assert.assertTrue(token.is(TokenTypes.MARKUP_COMMENT, "<!-- Hello world "));
		token = token.getNextToken();
		Assert.assertTrue(token.isHyperlink());
		Assert.assertTrue(token.is(TokenTypes.MARKUP_COMMENT, "http://www.google.com"));
		token = token.getNextToken();
		Assert.assertFalse(token.isHyperlink());
		Assert.assertTrue(token.is(TokenTypes.MARKUP_COMMENT, " -->"));

	}


	@Test
	public void testHtml_doctype() {

		String[] doctypes = {
			"<!doctype html>",
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">",
			"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
		};

		for (String code : doctypes) {
			Segment segment = new Segment(code.toCharArray(), 0, code.length());
			HTMLTokenMaker tm = new HTMLTokenMaker();
			Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);
			Assert.assertEquals(TokenTypes.MARKUP_DTD, token.getType());
		}

	}


	@Test
	public void testHtml_entityReferences() {

		String[] entityReferences = {
			"&nbsp;", "&lt;", "&gt;", "&#4012",
		};

		for (String code : entityReferences) {
			Segment segment = new Segment(code.toCharArray(), 0, code.length());
			HTMLTokenMaker tm = new HTMLTokenMaker();
			Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);
			Assert.assertEquals(TokenTypes.MARKUP_ENTITY_REFERENCE, token.getType());
		}

	}


	@Test
	public void testHtml_happyPath_tagWithAttributes() {

		String code = "<body onload=\"doSomething()\" data-extra='true'>";
		Segment segment = new Segment(code.toCharArray(), 0, code.length());
		HTMLTokenMaker tm = new HTMLTokenMaker();
		Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);

		Assert.assertTrue(token.isSingleChar(TokenTypes.MARKUP_TAG_DELIMITER, '<'));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_NAME, "body"));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.WHITESPACE, " "));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_ATTRIBUTE, "onload"));
		token = token.getNextToken();
		Assert.assertTrue(token.isSingleChar(TokenTypes.OPERATOR, '='));
		token = token.getNextToken();
		Assert.assertTrue("Unexpected token: " + token, token.is(TokenTypes.MARKUP_TAG_ATTRIBUTE_VALUE, "\"doSomething()\""));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.WHITESPACE, " "));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_ATTRIBUTE, "data-extra"));
		token = token.getNextToken();
		Assert.assertTrue(token.isSingleChar(TokenTypes.OPERATOR, '='));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_ATTRIBUTE_VALUE, "'true'"));
		token = token.getNextToken();
		Assert.assertTrue(token.isSingleChar(TokenTypes.MARKUP_TAG_DELIMITER, '>'));
		
	}


	@Test
	public void testHtml_happyPath_closedTag() {

		String code = "<img src='foo.png'/>";
		Segment segment = new Segment(code.toCharArray(), 0, code.length());
		HTMLTokenMaker tm = new HTMLTokenMaker();
		Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);

		Assert.assertTrue(token.isSingleChar(TokenTypes.MARKUP_TAG_DELIMITER, '<'));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_NAME, "img"));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.WHITESPACE, " "));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_ATTRIBUTE, "src"));
		token = token.getNextToken();
		Assert.assertTrue(token.isSingleChar(TokenTypes.OPERATOR, '='));
		token = token.getNextToken();
		Assert.assertTrue("Unexpected token: " + token, token.is(TokenTypes.MARKUP_TAG_ATTRIBUTE_VALUE, "'foo.png'"));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_DELIMITER, "/>"));
		
	}


	@Test
	public void testHtml_happyPath_closingTag() {

		String code = "</body>";
		Segment segment = new Segment(code.toCharArray(), 0, code.length());
		HTMLTokenMaker tm = new HTMLTokenMaker();
		Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);

		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_DELIMITER, "</"));
		token = token.getNextToken();
		Assert.assertTrue(token.is(TokenTypes.MARKUP_TAG_NAME, "body"));
		token = token.getNextToken();
		Assert.assertTrue(token.isSingleChar(TokenTypes.MARKUP_TAG_DELIMITER, '>'));

	}


	@Test
	public void testHtml_processingInstructions() {

		String[] doctypes = {
			"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
			"<?xml version='1.0' encoding='UTF-8' ?>",
			"<?xml-stylesheet type=\"text/css\" href=\"style.css\"?>",
		};

		for (String code : doctypes) {
			Segment segment = new Segment(code.toCharArray(), 0, code.length());
			HTMLTokenMaker tm = new HTMLTokenMaker();
			Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);
			Assert.assertEquals(TokenTypes.MARKUP_PROCESSING_INSTRUCTION, token.getType());
		}

	}


	@Test
	public void testHtml_validHtml5TagNames() {
		
		String[] tagNames = { 
			"a", "abbr", "acronym", "address", "applet", "area", "article",
			"aside", "audio", "b", "base", "basefont", "bdo", "bgsound", "big",
			"blink", "blockquote", "body", "br", "button", "canvas", "caption",
			"center", "cite", "code", "col", "colgroup", "command", "comment",
			"dd", "datagrid", "datalist", "datatemplate", "del", "details",
			"dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed",
			"eventsource", "fieldset", "figure", "font", "footer", "form",
			"frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6",
			"head", "header", "hr", "html", "i", "iframe", "ilayer", "img",
			"input", "ins", "isindex", "kbd", "keygen", "label", "layer",
			"legend", "li", "link", "map", "mark", "marquee", "menu", "meta",
			"meter", "multicol", "nav", "nest", "nobr", "noembed", "noframes",
			"nolayer", "noscript", "object", "ol", "optgroup", "option",
			"output", "p", "param", "plaintext", "pre", "progress", "q", "rule",
			"s", "samp", "script", "section", "select", "server", "small",
			"source", "spacer", "span", "strike", "strong", "style",
			"sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th",
			"thead", "time", "title", "tr", "tt", "u", "ul", "var", "video"
		};

		HTMLTokenMaker tm = new HTMLTokenMaker();
		for (String tagName : tagNames) {
			String text = "<" + tagName;
			Segment segment = new Segment(text.toCharArray(), 0, text.length());
			Token token = tm.getTokenList(segment, TokenTypes.NULL, 0);
			Assert.assertTrue(token.isSingleChar(TokenTypes.MARKUP_TAG_DELIMITER, '<'));
			token = token.getNextToken();
			Assert.assertTrue("Not a valid HTML5 tag name token: " + token,
					token.getType() == TokenTypes.MARKUP_TAG_NAME);
		}

	}


}